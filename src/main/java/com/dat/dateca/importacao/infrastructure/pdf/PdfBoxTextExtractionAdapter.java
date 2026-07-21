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
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extrai texto/páginas/imagens via Apache PDFBox. Cada página recebe dois sinais independentes
 * (ver {@link PageContent}): se tem texto nativo confiável e se o layout parece multi-coluna
 * (nesse caso o processador de importação prefere renderizar a página e usar visão, já que o
 * PDFTextStripper padrão intercala colunas ao ler da esquerda para a direita).
 */
@Component
public class PdfBoxTextExtractionAdapter implements PdfTextExtractionPort {

    private static final int MIN_CHARS_FOR_NATIVE_TEXT = 40;

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
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(pageNumber);
            stripper.setEndPage(pageNumber);
            String text = stripper.getText(document);

            boolean hasNativeText = text != null && text.trim().length() >= MIN_CHARS_FOR_NATIVE_TEXT;
            boolean hasEmbeddedImages = !extractEmbeddedImages(pdfBytes, pageNumber).isEmpty();
            boolean layoutAmbiguous = hasNativeText && looksMultiColumn(text);

            return new PageContent(pageNumber, text, hasNativeText, hasEmbeddedImages, layoutAmbiguous);
        } catch (IOException e) {
            throw new CorruptedPdfException("Não foi possível ler a página " + pageNumber + ": " + e.getMessage());
        }
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
