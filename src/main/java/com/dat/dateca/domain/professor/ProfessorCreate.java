package com.dat.dateca.domain.professor;

public record ProfessorCreate(
        Long registrationNumber,
        String name,
        String phone,
        String email
) {
}
