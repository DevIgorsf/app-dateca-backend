package com.dat.dateca.importacao.infrastructure.persistence;

import com.dat.dateca.importacao.domain.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {

    List<ImportJob> findByCreatedByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long createdByUserId);
}
