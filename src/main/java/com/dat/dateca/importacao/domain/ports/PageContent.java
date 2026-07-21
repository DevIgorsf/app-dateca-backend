package com.dat.dateca.importacao.domain.ports;

public record PageContent(int pageNumber, String nativeText, boolean hasNativeText,
                           boolean hasEmbeddedImages, boolean layoutAmbiguous) {

    public boolean needsVisionExtraction() {
        return !hasNativeText || layoutAmbiguous;
    }
}
