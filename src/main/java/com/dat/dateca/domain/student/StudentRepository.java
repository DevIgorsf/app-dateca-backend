package com.dat.dateca.domain.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    long count();

    Student findByRegistrationNumber(String registrationNumber);

    List<Student> findAllByOrderByPointsDesc();

    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR CAST(s.registrationNumber AS string) LIKE CONCAT('%', :query, '%')")
    Page<Student> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT s.id as id, s.name as name, s.points as points FROM Student s " +
            "WHERE s.id IN :ids ORDER BY s.points DESC, s.registrationNumber ASC")
    Page<RankingProjection> findRankingByIds(@Param("ids") Collection<UUID> ids, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.id IN :ids " +
            "AND (s.points > :points OR (s.points = :points AND s.registrationNumber < :registrationNumber))")
    long countHigherRankedAmong(@Param("ids") Collection<UUID> ids,
                                 @Param("points") int points,
                                 @Param("registrationNumber") Long registrationNumber);

    interface RankingProjection {
        UUID getId();
        String getName();
        int getPoints();
    }
}
