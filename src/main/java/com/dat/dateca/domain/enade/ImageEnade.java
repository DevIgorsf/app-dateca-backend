package com.dat.dateca.domain.enade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ImageEnade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Lob
    @Column(name = "imagem", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] imagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enade_id")
    private Enade enade;
}
