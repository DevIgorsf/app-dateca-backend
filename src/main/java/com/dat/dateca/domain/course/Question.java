package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
//@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private PointsEnum pointsEnum;
    private QuestionTypeEnum questionTypeEnum;
    @ManyToOne
    @JoinColumn(name="course_id", nullable=false)
    private Course course;
    @ManyToOne
    private Professor professorCreate;

    public Question(String name, PointsEnum pointsEnum, Course course, Professor professorCreate) {
        this.name = name;
        this.pointsEnum = pointsEnum;
        this.course= course;
        this.professorCreate = professorCreate;
    }
}
