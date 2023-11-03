package com.dat.dateca.domain.professor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    long count();

    Professor findByRegistrationNumber(String registrationNumber);
}
