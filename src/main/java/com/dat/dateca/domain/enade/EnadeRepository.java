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
    Optional<QuestionMultipleChoice> findRandomEnadeByIds(@Param("ids") List<Long> ids);
}
