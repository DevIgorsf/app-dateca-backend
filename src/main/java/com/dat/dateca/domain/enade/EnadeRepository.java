package com.dat.dateca.domain.enade;

import com.dat.dateca.domain.question.QuestionMultipleChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnadeRepository extends JpaRepository<Enade, Long> {

    long count();

    @Query("SELECT q.id FROM Enade q")
    List<Long> findIdsOfEnade();

    @Query("SELECT q FROM Enade q WHERE q.id IN :ids ORDER BY RAND() LIMIT 1")
    Optional<EnadeDTO> findRandomEnadeByIds(@Param("ids") List<Long> ids);

    @Query("SELECT q FROM Enade q LEFT JOIN q.images i WHERE i IS NULL")
    List<EnadeAllDTO> findAllEnadeWithoutImage();

    @Query("SELECT q FROM Enade q JOIN q.images i")
    List<EnadeAllDTO> findAllEnadeWithImage();

    @Query("SELECT new com.dat.dateca.domain.enade.EnadeRandDTO(" +
            "e.id, e.year, e.number, e.statement, e.alternativeA, " +
            "e.alternativeB, e.alternativeC, e.alternativeD, e.alternativeE) " +
            "FROM Enade e WHERE e.id = :id")
    Optional<EnadeRandDTO> findEnadeRandDTOById(@Param("id") Long id);
}
