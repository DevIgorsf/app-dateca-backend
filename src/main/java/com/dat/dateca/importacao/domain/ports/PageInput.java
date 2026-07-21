package com.dat.dateca.importacao.domain.ports;

public record PageInput(int pageNumber, String text, byte[] imageBytes, String imageMediaType) {

    public static PageInput ofText(int pageNumber, String text) {
        return new PageInput(pageNumber, text, null, null);
    }

    public static PageInput ofImage(int pageNumber, byte[] imageBytes, String imageMediaType) {
        return new PageInput(pageNumber, null, imageBytes, imageMediaType);
    }

    public boolean isImage() {
        return imageBytes != null;
    }
}
