package com.dat.dateca.domain.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(unique = true)
    private Long registrationNumber;
    private String name;
    private String phone;
    private String email;
    private int semester;

    public Student(StudentCadastro studentCadastro) {
        this.registrationNumber = studentCadastro.registrationNumber();
        this.name = studentCadastro.name();
        this.phone = studentCadastro.phone();
        this.email = studentCadastro.email();
    }
    int points;

    public void addPontuação(int key) {
        this.points += key;
    }

//    List<Question> questionList;
//    List<Course> courseList;
//    List<Student> friends;

}
