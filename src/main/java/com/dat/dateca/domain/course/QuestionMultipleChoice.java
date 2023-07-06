package com.dat.dateca.domain.course;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
@DiscriminatorValue("MultipleChoice")
public class QuestionMultipleChoice extends Question {
    List<String> choices;
    String correctAnswer;
}
