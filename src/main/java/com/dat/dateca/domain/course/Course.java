package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorDTO;
import com.dat.dateca.domain.question.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    public Course(CourseForm courseForm) {
        this.code = courseForm.code();
        this.name = courseForm.name();
        this.semester = courseForm.semester();
        this.professorList = courseForm.professorList();
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

    public List<ProfessorDTO> getProfessorListDTO() {
        return getProfessorList().stream().map(ProfessorDTO::new).toList();
    }
}
