package com.dat.dateca.domain.professor;

import com.dat.dateca.domain.course.Course;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Table(name = "professors")
@Entity(name = "professor")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Professor {

    @Id
    private Long id;
    private Long registrationNumber;
    private String name;
    private String phone;
    private String email;
    @ManyToMany
    List<Course> courseList;

    public Professor(ProfessorCreate professorCreate) {
        this.registrationNumber = professorCreate.registrationNumber();
        this.name = professorCreate.name();
        this.email = professorCreate.email();
        this.phone = professorCreate.phone();
    }
}
