package com.dat.dateca.domain.student;

public record StudentDTO(
        Long registrationNumber,
        String name,
        String phone,
        String email,
        int semester
) {
    public StudentDTO(Student student) {
        this(
                student.getRegistrationNumber(),
                student.getName(),
                student.getPhone(),
                student.getEmail(),
                student.getSemester()
        );
    }
}
