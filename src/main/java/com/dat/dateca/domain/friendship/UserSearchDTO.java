package com.dat.dateca.domain.friendship;

import com.dat.dateca.domain.student.Student;

import java.util.UUID;

public record UserSearchDTO(
        UUID id,
        Long registrationNumber,
        String name,
        long friendsCount,
        RelationshipStatus relationshipStatus
) {
    public UserSearchDTO(Student student, long friendsCount, RelationshipStatus relationshipStatus) {
        this(
                student.getId(),
                student.getRegistrationNumber(),
                student.getName(),
                friendsCount,
                relationshipStatus
        );
    }
}
