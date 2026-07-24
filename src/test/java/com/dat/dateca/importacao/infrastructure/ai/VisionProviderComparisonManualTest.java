package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.PageContent;
import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionBatch;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionResult;
import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;
import com.dat.dateca.importacao.infrastructure.pdf.PdfBoxTextExtractionAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Comparação MANUAL entre o provedor local (Ollama) e o remoto (Anthropic) para o MESMO PDF, no
 * caminho de texto nativo. Não roda na suíte normal — é uma ferramenta para você avaliar a
 * diferença de qualidade antes de decidir expandir o uso do modelo local.
 *
 * <p>Pré-requisitos: Ollama no ar com o modelo baixado e a variável {@code ANTHROPIC_API_KEY}
 * definida. Rode com:
 *
 * <pre>
 *   # PowerShell
 *   $env:RUN_VISION_COMPARISON = "true"; $env:ANTHROPIC_API_KEY = "sk-ant-..."
 *   ./mvnw test "-Dtest=VisionProviderComparisonManualTest"
 * </pre>
 *
 * Opcional: {@code OLLAMA_MODEL} para trocar o modelo local (default qwen2.5:7b-instruct-q4_K_M).
 */
@EnabledIfEnvironmentVariable(named = "RUN_VISION_COMPARISON", matches = "true")
class VisionProviderComparisonManualTest {

    private static final Path TEST_PDF = Path.of("src/test/resources/importacao/2025_caderno_1_preliminar.pdf");
    private static final int COVER_WINDOW = 3;
    private static final int QUESTION_WINDOW = 5;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PdfBoxTextExtractionAdapter pdf = new PdfBoxTextExtractionAdapter();

    @Test
    void compareOllamaVsAnthropicOnNativeTextPath() throws Exception {
        byte[] pdfBytes = Files.readAllBytes(TEST_PDF);

        int pageCount = pdf.countPages(pdfBytes);
        List<PageContent> pages = new ArrayList<>();
        for (int page = 1; page <= pageCount; page++) {
            pages.add(pdf.extractPage(pdfBytes, page));
        }

        // Só o caminho de texto nativo entra na comparação (é o que migra para local primeiro).
        List<PageInput> coverInputs = textInputs(pages, 0, COVER_WINDOW);
        List<PageInput> questionInputs = textInputs(pages, 0, QUESTION_WINDOW);

        if (questionInputs.isEmpty()) {
            System.out.println("[comparação] O PDF de teste não tem páginas de texto nativo suficientes; "
                    + "esse comparador cobre apenas o caminho de texto.");
            return;
        }

        VisionExtractionPort anthropic = new AnthropicVisionExtractionAdapter(anthropicProperties(), objectMapper);
        VisionExtractionPort ollama = new OllamaVisionExtractionAdapter(ollamaProperties(), objectMapper);

        System.out.println("================ COMPARAÇÃO OLLAMA x ANTHROPIC ================");
        System.out.println("PDF: " + TEST_PDF.getFileName() + " (" + pageCount + " páginas)");
        System.out.println("Páginas de texto nativo usadas: " + questionInputs.size());
        System.out.println();

        runProvider("ANTHROPIC", anthropic, coverInputs, questionInputs);
        System.out.println();
        runProvider("OLLAMA", ollama, coverInputs, questionInputs);
        System.out.println("==============================================================");
    }

    private void runProvider(String label, VisionExtractionPort port,
                             List<PageInput> coverInputs, List<PageInput> questionInputs) {
        System.out.println("----- " + label + " -----");
        long start = System.currentTimeMillis();
        try {
            CoverExtractionResult cover = port.extractCover(coverInputs);
            QuestionExtractionBatch batch = port.extractQuestions(questionInputs);
            long elapsed = System.currentTimeMillis() - start;

            System.out.printf("tempo: %.1fs%n", elapsed / 1000.0);
            System.out.println("capa.title: " + cover.title());
            System.out.println("capa.institution: " + cover.institution());
            System.out.println("capa.year: " + cover.year());
            System.out.println("truncado: " + batch.truncated());
            System.out.println("questões extraídas: " + batch.questions().size());
            for (QuestionExtractionResult q : batch.questions()) {
                System.out.printf("  Q%-3d | %d alternativas | resposta=%s | enunciado=%d chars%n",
                        q.number(), q.alternatives().size(),
                        q.correctAnswer() == null ? "-" : q.correctAnswer(),
                        q.statement() == null ? 0 : q.statement().length());
            }
        } catch (RuntimeException e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
    }

    private List<PageInput> textInputs(List<PageContent> pages, int from, int window) {
        List<PageInput> inputs = new ArrayList<>();
        int end = Math.min(pages.size(), from + window);
        for (int i = from; i < end; i++) {
            PageContent page = pages.get(i);
            if (!page.needsVisionExtraction()) {
                inputs.add(PageInput.ofText(page.pageNumber(), page.nativeText()));
            }
        }
        return inputs;
    }

    private AnthropicProperties anthropicProperties() {
        AnthropicProperties props = new AnthropicProperties();
        props.setApiKey(System.getenv("ANTHROPIC_API_KEY"));
        return props;
    }

    private OllamaProperties ollamaProperties() {
        OllamaProperties props = new OllamaProperties();
        String model = System.getenv("OLLAMA_MODEL");
        if (model != null && !model.isBlank()) {
            props.setModel(model);
        }
        String baseUrl = System.getenv("OLLAMA_BASE_URL");
        if (baseUrl != null && !baseUrl.isBlank()) {
            props.setBaseUrl(baseUrl);
        }
        return props;
    }
}
