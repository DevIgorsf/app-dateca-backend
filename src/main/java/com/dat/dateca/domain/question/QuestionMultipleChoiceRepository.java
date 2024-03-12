package com.dat.dateca.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface QuestionMultipleChoiceRepository extends JpaRepository<QuestionMultipleChoice, Long> {

    long count();

    @Query("SELECT q.id FROM QuestionMultipleChoice q WHERE q.questionTypeEnum = 'MULTIPLE_CHOICE'")
    List<Long> findIdsOfMultipleChoiceQuestions();
    @Query("SELECT q FROM QuestionMultipleChoice q WHERE q.id IN :ids ORDER BY RAND() LIMIT 1")
    Optional<QuestionMultipleChoice> findRandomQuestionByIds(@Param("ids") List<Long> ids);

    List<QuestionMultipleChoice> findByidImagesIsEmpty();

    List<QuestionMultipleChoice> findByidImagesIsNotEmpty();
}
