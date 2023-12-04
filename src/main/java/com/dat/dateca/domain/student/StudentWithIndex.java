package com.dat.dateca.domain.student;

public record StudentWithIndex (
        Student student,
        int index
) {
    public StudentWithIndex(Student student, int index) {
        this.student = student;
        this.index = index;
    }
}
