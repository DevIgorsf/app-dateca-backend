package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.ProfessorDTO;

import java.util.List;

public record CourseDTO(
        Long id,
        String code,
        String name,
        int semester,
        List<ProfessorDTO> professorList
) {
    public CourseDTO(Course courseSaved) {
        this(
                courseSaved.getId(),
                courseSaved.getCode(),
                courseSaved.getName(),
                courseSaved.getSemester(),
                courseSaved.getProfessorListDTO());
    }
}
