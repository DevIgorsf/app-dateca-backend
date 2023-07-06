package com.dat.dateca.domain.professor;

public record ProfessorCreateDTO(
        Long registrationNumber,
        String name,
        String phone,
        String email) {

    public ProfessorCreateDTO(Professor professor) {
        this(professor.getRegistrationNumber(), professor.getName(), professor.getEmail(), professor.getPhone());
    }
}
