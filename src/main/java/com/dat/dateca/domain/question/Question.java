package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.question.QuestionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String statement;
    @Enumerated(EnumType.STRING)
    private PointsEnum pointsEnum;
    @Enumerated(EnumType.STRING)
    private QuestionTypeEnum questionTypeEnum;
    @ManyToOne
    @JoinColumn(name="course_id", nullable=false)
    private Course course;
    @ManyToOne
    private Professor professorCreate;

    public Question(String statement, PointsEnum pointsEnum, QuestionTypeEnum questionTypeEnum, Course course, Professor professorCreate) {
        this.statement = statement;
        this.pointsEnum = pointsEnum;
        this.questionTypeEnum = questionTypeEnum;
        this.course= course;
        this.professorCreate = professorCreate;
    }
}
