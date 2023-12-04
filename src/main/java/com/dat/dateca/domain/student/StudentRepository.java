package com.dat.dateca.domain.student;

import com.dat.dateca.domain.professor.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Student findByRegistrationNumber(String registrationNumber);

    List<Student> findAllByOrderByPointsDesc();
}
