package com.dat.dateca.importacao.infrastructure.pdf;

import com.dat.dateca.importacao.domain.ports.ExtractedImage;
import com.dat.dateca.importacao.domain.ports.PageContent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste "golden file" contra um PDF real (prova do ENAMED, caderno preliminar) em
 * src/test/resources/importacao — deterministico, sem envolver IA. Serve para validar a
 * extração de texto/imagens do PDFBox, incluindo a correção de layout em duas colunas
 * (ver comentário em {@link PdfBoxTextExtractionAdapter}), contra um documento real.
 *
 * <p>Os valores esperados abaixo (contagem de páginas, texto por página, presença de imagens)
 * foram confirmados rodando o adapter contra o arquivo real antes de escrever as asserções.
 */
class PdfBoxTextExtractionAdapterTest {

    private static final String RESOURCE_PATH = "/importacao/2025_caderno_1_preliminar.pdf";
    private static final int EXPECTED_PAGE_COUNT = 28;

    private static byte[] pdfBytes;

    private final PdfBoxTextExtractionAdapter adapter = new PdfBoxTextExtractionAdapter();

    @BeforeAll
    static void loadFixture() throws IOException {
        try (InputStream input = PdfBoxTextExtractionAdapterTest.class.getResourceAsStream(RESOURCE_PATH)) {
            assertThat(input)
                    .as("Arquivo de teste %s não encontrado em src/test/resources", RESOURCE_PATH)
                    .isNotNull();
            pdfBytes = input.readAllBytes();
        }
    }

    @Test
    void countsAllPagesOfTheRealExam() {
        assertThat(adapter.countPages(pdfBytes)).isEqualTo(EXPECTED_PAGE_COUNT);
    }

    @Test
    void blankPageAfterTheCoverHasNoNativeText() {
        PageContent page2 = adapter.extractPage(pdfBytes, 2);

        assertThat(page2.hasNativeText()).isFalse();
        assertThat(page2.needsVisionExtraction()).isTrue();
    }

    @Test
    void coverPageHasNativeTextAndEmbeddedGraphics() {
        PageContent page1 = adapter.extractPage(pdfBytes, 1);

        assertThat(page1.hasNativeText()).isTrue();
        assertThat(page1.hasEmbeddedImages()).isTrue();
    }

    @Test
    void twoColumnPageIsReassembledInReadingOrder() {
        PageContent page3 = adapter.extractPage(pdfBytes, 3);

        assertThat(page3.hasNativeText()).isTrue();
        assertThat(page3.layoutAmbiguous())
                .as("a detecção de coluna deveria corrigir a ordem de leitura, dispensando o fallback de ambiguidade")
                .isFalse();

        String text = page3.nativeText();
        // Trechos em ASCII puro (sem acentuação) para não depender de charset do compilador/console.
        assertThat(text).contains("Mulher de 58 anos");
        assertThat(text).contains("espironolactona");

        // A questao 1 completa (enunciado + 4 alternativas) precisa aparecer antes da questao 2
        // comecar, confirmando que a coluna esquerda foi lida por inteiro antes da direita.
        int questao1Start = text.indexOf("Mulher de 58 anos");
        int alternativaD = text.indexOf("clonidina");
        int questao2Start = text.indexOf("Lactente de 4 meses");
        assertThat(questao1Start).isGreaterThanOrEqualTo(0);
        assertThat(alternativaD).isGreaterThan(questao1Start);
        assertThat(questao2Start).isGreaterThan(alternativaD);
    }

    @Test
    void annulledQuestionPageContainsTheAnuladaMark() {
        PageContent page4 = adapter.extractPage(pdfBytes, 4);

        assertThat(page4.hasNativeText()).isTrue();
        assertThat(page4.nativeText()).contains("ANULADA");
    }

    @Test
    void questionPagesContainEmbeddedFigures() {
        List<ExtractedImage> imagesOnPage3 = adapter.extractEmbeddedImages(pdfBytes, 3);

        assertThat(imagesOnPage3).isNotEmpty();
        assertThat(imagesOnPage3.get(0).contentType()).isEqualTo("image/png");
        assertThat(imagesOnPage3.get(0).content()).isNotEmpty();
    }

    @Test
    void rasterizingAPageProducesNonEmptyPng() {
        byte[] rendered = adapter.rasterizePage(pdfBytes, 1, 100);

        assertThat(rendered).isNotEmpty();
        // Assinatura PNG: 89 50 4E 47 0D 0A 1A 0A
        assertThat(rendered[0]).isEqualTo((byte) 0x89);
        assertThat(rendered[1]).isEqualTo((byte) 0x50);
        assertThat(rendered[2]).isEqualTo((byte) 0x4E);
        assertThat(rendered[3]).isEqualTo((byte) 0x47);
    }
}
