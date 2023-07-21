package com.dat.dateca.domain.professor;

public record ProfessorUpdate(
        Long registrationNumber,
        String name,
        String phone,
        String email) {
}
