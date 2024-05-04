package com.dat.dateca.domain.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    long count();

    Student findByRegistrationNumber(String registrationNumber);

    List<Student> findAllByOrderByPointsDesc();
}
