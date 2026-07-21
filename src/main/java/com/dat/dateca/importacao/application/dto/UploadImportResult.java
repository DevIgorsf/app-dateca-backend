package com.dat.dateca.importacao.application.dto;

import com.dat.dateca.importacao.domain.ImportStatus;

import java.util.UUID;

public record UploadImportResult(UUID importJobId, ImportStatus status) {
}
