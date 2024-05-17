package com.dat.dateca.domain.enade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnadeAnswerResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "enade_id", referencedColumnName = "id")
    private Enade enade;

    private boolean correct;

    public EnadeAnswerResult(Enade enade, boolean correct) {
        this.enade = enade;
        this.correct = correct;
    }
}
