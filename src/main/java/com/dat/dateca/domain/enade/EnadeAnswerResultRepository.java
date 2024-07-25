package com.dat.dateca.domain.enade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnadeAnswerResultRepository  extends JpaRepository<EnadeAnswerResult, Long> {

    @Query("SELECT count(e) FROM EnadeAnswerResult e WHERE e.correct = TRUE")
    Long countCorrect();
}
