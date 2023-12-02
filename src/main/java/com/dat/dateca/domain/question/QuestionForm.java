package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.question.QuestionTypeEnum;
import jakarta.annotation.Nullable;
import lombok.Setter;

import java.util.List;

public record QuestionForm(
        @Nullable
        Long id,
        String statement,
        PointsEnum pointsEnum,
        Long course,
        Character correctAnswer,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE
) {
}
