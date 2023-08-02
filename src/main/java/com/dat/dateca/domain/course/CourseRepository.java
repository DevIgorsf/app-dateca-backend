package com.dat.dateca.domain.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByProfessorListId(Long professorId);

    long count();
}
