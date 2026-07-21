package com.dat.dateca.exam.infrastructure.persistence;

import com.dat.dateca.exam.domain.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    Page<Exam> findAllByOrderByPublishedAtDesc(Pageable pageable);
}
