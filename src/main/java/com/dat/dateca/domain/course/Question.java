package com.dat.dateca.domain.course;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public abstract class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    String name;
    PointsEnum pointsEnum;
    QuestionTypeEnum questionTypeEnum;
}
