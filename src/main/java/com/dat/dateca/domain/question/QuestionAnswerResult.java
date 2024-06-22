package com.dat.dateca.domain.question;

import jakarta.persistence.*;

@Entity
public class QuestionAnswerResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private Question question;

    private boolean correct;

    public QuestionAnswerResult(Question question, boolean correct) {
        this.question = question;
        this.correct = correct;
    }

    public boolean getCorrect() {
        return this.correct;
    }
}
