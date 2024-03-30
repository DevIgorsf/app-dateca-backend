package com.dat.dateca.domain.enade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageEnadeRepository extends JpaRepository<ImageEnade, Long> {

    List<ImageEnade> findByEnadeId(Long enadeId);
}
