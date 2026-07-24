package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionBatch;
import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;

import java.util.List;
import java.util.Map;

/**
 * Roteia cada chamada entre um provedor de <em>texto</em> (ex.: Ollama local) e um provedor de
 * <em>visão</em> (ex.: Anthropic), por página. Se o conjunto de páginas contém qualquer imagem
 * rasterizada, a chamada inteira vai para o provedor de visão — é o caminho mais exigente em
 * qualidade e ainda não validado localmente. Blocos 100% de texto nativo ficam no provedor local.
 *
 * <p>Transparente para o {@code PdfImportProcessor}: o contrato de {@link VisionExtractionPort} e o
 * formato de saída não mudam.
 */
public class RoutingVisionExtractionAdapter implements VisionExtractionPort {

    private final VisionExtractionPort textDelegate;
    private final VisionExtractionPort visionDelegate;

    public RoutingVisionExtractionAdapter(VisionExtractionPort textDelegate, VisionExtractionPort visionDelegate) {
        this.textDelegate = textDelegate;
        this.visionDelegate = visionDelegate;
    }

    @Override
    public CoverExtractionResult extractCover(List<PageInput> coverPages) {
        return route(coverPages).extractCover(coverPages);
    }

    @Override
    public QuestionExtractionBatch extractQuestions(List<PageInput> chunkPages) {
        return route(chunkPages).extractQuestions(chunkPages);
    }

    @Override
    public Map<Integer, Character> extractAnswerKey(List<PageInput> answerKeyPages) {
        return route(answerKeyPages).extractAnswerKey(answerKeyPages);
    }

    private VisionExtractionPort route(List<PageInput> pages) {
        boolean hasImage = pages.stream().anyMatch(PageInput::isImage);
        return hasImage ? visionDelegate : textDelegate;
    }
}
