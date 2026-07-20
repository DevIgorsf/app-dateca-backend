package com.dat.dateca.domain.friendship;

import com.dat.dateca.domain.student.Student;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendDTO(
        UUID id,
        Long registrationNumber,
        String name,
        int points,
        LocalDateTime friendSince
) {
    public FriendDTO(Student student, LocalDateTime friendSince) {
        this(
                student.getId(),
                student.getRegistrationNumber(),
                student.getName(),
                student.getPoints(),
                friendSince
        );
    }
}
