package com.dat.dateca.domain.question;


public record ImageQuestionDTO(
        Long id,
        String nome,
        byte[] imagem
) {
    public ImageQuestionDTO(ImageQuestion imageQuestion) {
        this(
                imageQuestion.getId(),
                imageQuestion.getNome(),
                imageQuestion.getImagem()
        );
    }
}
