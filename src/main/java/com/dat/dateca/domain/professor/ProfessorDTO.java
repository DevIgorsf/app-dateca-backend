package com.dat.dateca.domain.professor;

public record ProfessorDTO(
        Long id,
        Long registrationNumber,
        String name,
        String phone,
        String email) {

    public ProfessorDTO(Professor professor) {
        this(professor.getId(), professor.getRegistrationNumber(), professor.getName(), professor.getPhone(), professor.getEmail());
    }
}
