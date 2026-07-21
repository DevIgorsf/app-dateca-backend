package com.dat.dateca.importacao.infrastructure.async;

import com.dat.dateca.importacao.application.PdfImportProcessor;
import com.dat.dateca.importacao.domain.ports.ImportJobDispatcher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementação in-process (thread pool dedicado) do disparo assíncrono de importações.
 * Ponto único de troca futura por mensageria: {@link PdfImportProcessor#process(UUID)} é
 * idempotente/retomável por id (não depende de estado em memória), então uma implementação
 * futura baseada em fila (ex.: RabbitMQ) só precisa publicar o {@code importJobId} e ter um
 * consumidor chamando o mesmo método — nada no processador muda.
 */
@Component
public class InProcessImportJobDispatcher implements ImportJobDispatcher {

    private final PdfImportProcessor pdfImportProcessor;

    public InProcessImportJobDispatcher(PdfImportProcessor pdfImportProcessor) {
        this.pdfImportProcessor = pdfImportProcessor;
    }

    @Override
    @Async("importTaskExecutor")
    public void dispatch(UUID importJobId) {
        pdfImportProcessor.process(importJobId);
    }
}
