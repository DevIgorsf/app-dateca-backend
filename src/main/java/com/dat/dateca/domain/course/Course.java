package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    @Column(unique = true)
    private String code;
    String name;
    int semester;
    List<Professor> professorList;
    List<Question> questionList;
}
