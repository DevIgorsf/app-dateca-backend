package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;

import java.util.List;

public record CourseForm(
        Long id,
        String code,
        String name,
        int semester,
        List<Professor> professorList
) {
    public CourseForm(Course courseSaved) {
        this(
                courseSaved.getId(),
                courseSaved.getCode(),
                courseSaved.getName(),
                courseSaved.getSemester(),
                courseSaved.getProfessorList());
    }
}
