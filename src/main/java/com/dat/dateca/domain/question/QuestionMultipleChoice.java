package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
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
    @Lob
    @Column(columnDefinition = "TEXT")
    private String alternativeA;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String alternativeB;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String alternativeC;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String alternativeD;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String alternativeE;

    public QuestionMultipleChoice(QuestionForm questionForm, Professor professorCreate, Course course) {
        super(questionForm.statement(), questionForm.pointsEnum(), QuestionTypeEnum.MULTIPLE_CHOICE, course, professorCreate);
        this.correctAnswer = questionForm.correctAnswer();
        this.alternativeA = questionForm.alternativeA();
        this.alternativeB = questionForm.alternativeB();
        this.alternativeC = questionForm.alternativeC();
        this.alternativeD = questionForm.alternativeD();
        this.alternativeE = questionForm.alternativeE();
    }

    public QuestionMultipleChoice(String statement, List<Long> imagensSalvas, PointsEnum pointsEnum, Course course, Professor professor, Character correctAnswer, String alternativeA, String alternativeB, String alternativeC, String alternativeD, String alternativeE) {
        super(statement, pointsEnum, QuestionTypeEnum.MULTIPLE_CHOICE, course, professor);
        this.idImages = imagensSalvas;
        this.correctAnswer = correctAnswer;
        this.alternativeA = alternativeA;
        this.alternativeB = alternativeB;
        this.alternativeC = alternativeC;
        this.alternativeD = alternativeD;
        this.alternativeE = alternativeE;
    }

    public void updateQuestionMultipleChoice(QuestionForm questionForm, Course course) {
        super.updateQuestion(questionForm.statement(), questionForm.pointsEnum(), course);
        this.correctAnswer = questionForm.correctAnswer();
        this.alternativeA = questionForm.alternativeA();
        this.alternativeB = questionForm.alternativeB();
        this.alternativeC = questionForm.alternativeC();
        this.alternativeD = questionForm.alternativeD();
        this.alternativeE = questionForm.alternativeE();
    }
}
