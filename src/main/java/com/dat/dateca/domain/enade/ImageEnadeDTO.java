package com.dat.dateca.domain.enade;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

public record ImageEnadeDTO(
        Long id,
        String nome,
        byte[] imagem
) {
    public ImageEnadeDTO(ImageEnade imageEnade) {
        this(
                imageEnade.getId(),
                imageEnade.getNome(),
                imageEnade.getImagem()
        );
    }
}
