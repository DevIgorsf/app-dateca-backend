package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;

import java.util.List;

public record QuestionForm(
        Long id,
        String name,
        PointsEnum pointsEnum,
        QuestionTypeEnum questionTypeEnum,
        Course course,
        Professor professorCreate,
        List<String> choices,
        String correctAnswer
) {
}
