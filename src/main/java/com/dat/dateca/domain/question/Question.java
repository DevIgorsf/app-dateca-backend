package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.course.CourseDTO;
import com.dat.dateca.domain.enade.ImageEnade;
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
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
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

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImageQuestion> images;

    public Question(String statement, PointsEnum pointsEnum, QuestionTypeEnum questionTypeEnum, Course course, Professor professorCreate) {
        this.statement = statement;
        this.pointsEnum = pointsEnum;
        this.questionTypeEnum = questionTypeEnum;
        this.course= course;
        this.professorCreate = professorCreate;
    }

    public CourseDTO getCourseDTO() {
        return new CourseDTO(this.getCourse());
    }

    protected void updateQuestion(String statement, PointsEnum pointsEnum, Course course) {
        this.statement = statement;
        this.pointsEnum = pointsEnum;
        this.course = course;
    }
}
