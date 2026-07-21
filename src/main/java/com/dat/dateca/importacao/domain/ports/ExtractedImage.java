package com.dat.dateca.importacao.domain.ports;

public record ExtractedImage(byte[] content, String contentType, int ordem) {
}
