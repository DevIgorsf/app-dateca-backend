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

    private List<Long> idImages;

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
        this.idImages = questionForm.idImages();
        this.correctAnswer = questionForm.correctAnswer();
        this.alternativeA = questionForm.alternativeA();
        this.alternativeB = questionForm.alternativeB();
        this.alternativeC = questionForm.alternativeC();
        this.alternativeD = questionForm.alternativeD();
        this.alternativeE = questionForm.alternativeE();
    }

    public void updateQuestionMultipleChoice(QuestionForm questionForm, Course course) {
        super.updateQuestion(questionForm.statement(), questionForm.pointsEnum(), course);
        this.idImages = questionForm.idImages();
        this.correctAnswer = questionForm.correctAnswer();
        this.alternativeA = questionForm.alternativeA();
        this.alternativeB = questionForm.alternativeB();
        this.alternativeC = questionForm.alternativeC();
        this.alternativeD = questionForm.alternativeD();
        this.alternativeE = questionForm.alternativeE();
    }
}
