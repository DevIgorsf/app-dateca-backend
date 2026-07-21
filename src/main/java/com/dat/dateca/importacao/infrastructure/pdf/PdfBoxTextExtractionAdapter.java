package com.dat.dateca.importacao.infrastructure.pdf;

import com.dat.dateca.importacao.domain.exceptions.CorruptedPdfException;
import com.dat.dateca.importacao.domain.ports.ExtractedImage;
import com.dat.dateca.importacao.domain.ports.PageContent;
import com.dat.dateca.importacao.domain.ports.PdfTextExtractionPort;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extrai texto/páginas/imagens via Apache PDFBox. Provas em formato de caderno costumam usar
 * layout em duas colunas — o {@link PDFTextStripper} padrão (mesmo com sortByPosition) lê a
 * página inteira da esquerda para a direita por faixa vertical, intercalando o fim de uma frase
 * da coluna esquerda com o início da frase correspondente da coluna direita. Em vez de só
 * sinalizar isso como "ambíguo" e depender da IA para desembaralhar, detectamos a rua entre
 * colunas pela distribuição horizontal dos caracteres e extraímos cada coluna separadamente
 * (coluna esquerda de cima a baixo, depois a direita) — corrigindo a ordem de leitura na origem.
 */
@Component
public class PdfBoxTextExtractionAdapter implements PdfTextExtractionPort {

    private static final int MIN_CHARS_FOR_NATIVE_TEXT = 40;
    private static final int MIN_CHARACTERS_FOR_COLUMN_DETECTION = 200;
    private static final int GUTTER_BUCKET_COUNT = 40;
    private static final float GUTTER_SEARCH_START_RATIO = 0.3f;
    private static final float GUTTER_SEARCH_END_RATIO = 0.7f;

    @Override
    public int countPages(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new CorruptedPdfException("Não foi possível abrir o PDF: " + e.getMessage());
        }
    }

    @Override
    public PageContent extractPage(byte[] pdfBytes, int pageNumber) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            Float gutterX = detectColumnGutter(document, pageNumber);
            String text;
            boolean layoutAmbiguous;

            if (gutterX != null) {
                text = extractByColumns(document, pageNumber, gutterX);
                layoutAmbiguous = false;
            } else {
                text = extractSinglePass(document, pageNumber);
                layoutAmbiguous = text != null && looksMultiColumn(text);
            }

            boolean hasNativeText = text != null && text.trim().length() >= MIN_CHARS_FOR_NATIVE_TEXT;
            boolean hasEmbeddedImages = !extractEmbeddedImages(pdfBytes, pageNumber).isEmpty();

            return new PageContent(pageNumber, text, hasNativeText, hasEmbeddedImages, layoutAmbiguous);
        } catch (IOException e) {
            throw new CorruptedPdfException("Não foi possível ler a página " + pageNumber + ": " + e.getMessage());
        }
    }

    private String extractSinglePass(PDDocument document, int pageNumber) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(pageNumber);
        stripper.setEndPage(pageNumber);
        return stripper.getText(document);
    }

    /**
     * Varre as posições X de todos os caracteres da página e procura um "vale" (poucos
     * caracteres) numa faixa central, com conteúdo denso dos dois lados — sinal de duas colunas
     * separadas por uma rua em branco. Retorna a coordenada X do centro dessa rua, ou
     * {@code null} se a página não parecer ter duas colunas (ex.: capa, página de gabarito).
     */
    private Float detectColumnGutter(PDDocument document, int pageNumber) throws IOException {
        List<Float> xPositions = new ArrayList<>();
        PDFTextStripper collector = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) {
                for (TextPosition position : textPositions) {
                    xPositions.add(position.getX());
                }
            }
        };
        collector.setStartPage(pageNumber);
        collector.setEndPage(pageNumber);
        collector.getText(document);

        if (xPositions.size() < MIN_CHARACTERS_FOR_COLUMN_DETECTION) {
            return null;
        }

        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        for (float x : xPositions) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }

        float bucketWidth = (maxX - minX) / GUTTER_BUCKET_COUNT;
        if (bucketWidth <= 0) {
            return null;
        }

        int[] histogram = new int[GUTTER_BUCKET_COUNT];
        for (float x : xPositions) {
            int bucket = Math.min(GUTTER_BUCKET_COUNT - 1, (int) ((x - minX) / bucketWidth));
            histogram[bucket]++;
        }

        int averagePerBucket = xPositions.size() / GUTTER_BUCKET_COUNT;
        int valleyThreshold = Math.max(1, averagePerBucket / 4);
        int denseThreshold = averagePerBucket / 2;

        int searchStart = (int) (GUTTER_BUCKET_COUNT * GUTTER_SEARCH_START_RATIO);
        int searchEnd = (int) (GUTTER_BUCKET_COUNT * GUTTER_SEARCH_END_RATIO);

        for (int i = searchStart; i <= searchEnd; i++) {
            if (histogram[i] > valleyThreshold) {
                continue;
            }
            boolean hasDenseContentBefore = Arrays.stream(histogram, 0, i).anyMatch(count -> count > denseThreshold);
            boolean hasDenseContentAfter = Arrays.stream(histogram, i + 1, GUTTER_BUCKET_COUNT).anyMatch(count -> count > denseThreshold);
            if (hasDenseContentBefore && hasDenseContentAfter) {
                return minX + (i + 0.5f) * bucketWidth;
            }
        }
        return null;
    }

    private String extractByColumns(PDDocument document, int pageNumber, float gutterX) throws IOException {
        PDPage page = document.getPage(pageNumber - 1);
        PDRectangle pageSize = page.getMediaBox();

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        stripper.addRegion("left", new Rectangle2D.Float(0, 0, gutterX, pageSize.getHeight()));
        stripper.addRegion("right", new Rectangle2D.Float(gutterX, 0, pageSize.getWidth() - gutterX, pageSize.getHeight()));
        stripper.extractRegions(page);

        String left = stripper.getTextForRegion("left");
        String right = stripper.getTextForRegion("right");
        return left + "\n" + right;
    }

    @Override
    public byte[] rasterizePage(byte[] pdfBytes, int pageNumber, int dpi) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(pageNumber - 1, dpi);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new CorruptedPdfException("Não foi possível renderizar a página " + pageNumber + ": " + e.getMessage());
        }
    }

    @Override
    public List<ExtractedImage> extractEmbeddedImages(byte[] pdfBytes, int pageNumber) {
        List<ExtractedImage> images = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
                return images;
            }
            PDPage page = document.getPage(pageNumber - 1);
            collectImages(page.getResources(), images);
            return images;
        } catch (IOException e) {
            throw new CorruptedPdfException("Não foi possível varrer imagens da página " + pageNumber + ": " + e.getMessage());
        }
    }

    private void collectImages(PDResources resources, List<ExtractedImage> images) throws IOException {
        if (resources == null) {
            return;
        }
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(name);
            if (xObject instanceof PDImageXObject imageXObject) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageIO.write(imageXObject.getImage(), "png", output);
                images.add(new ExtractedImage(output.toByteArray(), "image/png", images.size()));
            } else if (xObject instanceof PDFormXObject formXObject) {
                collectImages(formXObject.getResources(), images);
            }
        }
    }

    private boolean looksMultiColumn(String text) {
        String[] lines = text.split("\n");
        if (lines.length < 10) {
            return false;
        }
        long shortLines = Arrays.stream(lines)
                .filter(l -> !l.strip().isEmpty() && l.strip().length() < 15)
                .count();
        return shortLines > (lines.length * 0.5);
    }
}
