package com.dat.dateca.domain.student;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    @Column(unique = true)
    private Long registrationNumber;
    String name;
    String phone;
    String email;
    int semester;
//    int points;
//    List<Question> questionList;
//    List<Course> courseList;
//    List<Student> friends;

}
