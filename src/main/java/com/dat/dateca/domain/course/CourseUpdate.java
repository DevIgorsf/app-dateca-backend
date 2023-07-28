package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;

import java.util.List;

public record CourseUpdate(
        String code,
        String name,
        int semester,
        List<Professor> professorList
) {
}
