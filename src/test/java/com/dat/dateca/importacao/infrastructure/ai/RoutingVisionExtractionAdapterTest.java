package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionBatch;
import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingVisionExtractionAdapterTest {

    @Mock
    private VisionExtractionPort textDelegate;

    @Mock
    private VisionExtractionPort visionDelegate;

    @Test
    void routesTextOnlyPagesToTextDelegate() {
        RoutingVisionExtractionAdapter routing = new RoutingVisionExtractionAdapter(textDelegate, visionDelegate);
        List<PageInput> textPages = List.of(PageInput.ofText(1, "abc"), PageInput.ofText(2, "def"));
        when(textDelegate.extractQuestions(textPages))
                .thenReturn(QuestionExtractionBatch.complete(List.of()));

        routing.extractQuestions(textPages);

        verify(textDelegate).extractQuestions(textPages);
        verifyNoInteractions(visionDelegate);
    }

    @Test
    void routesToVisionDelegateWhenAnyPageIsImage() {
        RoutingVisionExtractionAdapter routing = new RoutingVisionExtractionAdapter(textDelegate, visionDelegate);
        List<PageInput> mixedPages = List.of(
                PageInput.ofText(1, "abc"),
                PageInput.ofImage(2, new byte[]{1, 2, 3}, "image/png"));
        when(visionDelegate.extractQuestions(mixedPages))
                .thenReturn(QuestionExtractionBatch.complete(List.of()));

        routing.extractQuestions(mixedPages);

        verify(visionDelegate).extractQuestions(mixedPages);
        verifyNoInteractions(textDelegate);
    }

    @Test
    void routesCoverIndependentlyPerCall() {
        RoutingVisionExtractionAdapter routing = new RoutingVisionExtractionAdapter(textDelegate, visionDelegate);
        List<PageInput> scannedCover = List.of(PageInput.ofImage(1, new byte[]{9}, "image/png"));
        when(visionDelegate.extractCover(scannedCover))
                .thenReturn(new CoverExtractionResult("t", null, null, null, null, "{}"));

        routing.extractCover(scannedCover);

        verify(visionDelegate).extractCover(any());
        verifyNoInteractions(textDelegate);
    }
}
