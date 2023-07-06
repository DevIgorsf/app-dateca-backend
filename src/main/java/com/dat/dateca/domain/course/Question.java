package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
public abstract class Question {

    @Id
    private Long id;
    private String name;
    private PointsEnum pointsEnum;
    private QuestionTypeEnum questionTypeEnum;
    @ManyToOne
    private Professor professorCreate;
}
