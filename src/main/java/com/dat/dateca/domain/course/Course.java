package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String code;
    String name;
    int semester;
    @ManyToMany
    List<Professor> professorList;
    @OneToMany(mappedBy="course")
    List<Question> questionList;

    public Course(CourseDTO courseDTO) {
        this.code = courseDTO.code();
        this.name = courseDTO.name();
        this.semester = courseDTO.semester();
        this.professorList = courseDTO.professorList();
    }

    public void updateCourse(CourseUpdate courseUpdate) {
        if(courseUpdate.code() != null) {
            this.code = courseUpdate.code();
        }
        if(courseUpdate.name() != null) {
            this.name = courseUpdate.name();
        }
        if(courseUpdate.semester() != ' ') {
            this.semester = courseUpdate.semester();
        }
        if(!courseUpdate.professorList().isEmpty()) {
            this.professorList = courseUpdate.professorList();
        }
    }
}
