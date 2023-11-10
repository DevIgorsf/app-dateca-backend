package com.dat.dateca.domain.question;

import com.dat.dateca.domain.professor.Professor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name="id")
public class QuestionMultipleChoice extends Question {

    private Character correctAnswer;
    private String alternativeA;
    private String alternativeB;
    private String alternativeC;
    private String alternativeD;
    private String alternativeE;

    public QuestionMultipleChoice(QuestionForm questionForm, Professor professorCreate) {
        super(questionForm.statement(), questionForm.pointsEnum(), QuestionTypeEnum.MULTIPLE_CHOICE, questionForm.course(), professorCreate);
        this.correctAnswer = questionForm.correctAnswer();
        this.alternativeA = questionForm.alternativeA();
        this.alternativeB = questionForm.alternativeB();
        this.alternativeC = questionForm.alternativeC();
        this.alternativeD = questionForm.alternativeD();
        this.alternativeE = questionForm.alternativeE();
    }

}
