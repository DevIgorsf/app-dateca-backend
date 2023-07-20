package com.dat.dateca.domain.professor;

public record ProfessorCreateDTO(
        Long id,
        Long registrationNumber,
        String name,
        String phone,
        String email) {

    public ProfessorCreateDTO(Professor professor) {
        this(professor.getId(), professor.getRegistrationNumber(), professor.getName(), professor.getPhone(), professor.getEmail());
    }
}
