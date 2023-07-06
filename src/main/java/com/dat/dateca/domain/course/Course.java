package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
public class Course {

    @Id
    private Long id;
    @Column(unique = true)
    private String code;
    String name;
    int semester;
    @ManyToMany
    List<Professor> professorList;
    @ManyToMany
    List<Question> questionList;
}
