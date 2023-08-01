package com.dat.dateca.domain.course;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("MultipleChoice")
//@PrimaryKeyJoinColumn(name = "id")
public class QuestionMultipleChoice extends Question {
    @ElementCollection
    List<String> choices;
    String correctAnswer;


    public QuestionMultipleChoice(QuestionForm questionForm) {
        super(questionForm.name(), questionForm.pointsEnum(), questionForm.course(), questionForm.professorCreate());
        this.choices = questionForm.choices();
        this.correctAnswer = questionForm.correctAnswer();
    }

}
