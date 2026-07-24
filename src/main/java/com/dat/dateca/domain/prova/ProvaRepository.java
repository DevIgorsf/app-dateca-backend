package com.dat.dateca.domain.prova;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProvaRepository extends JpaRepository<Prova, UUID> {

    List<Prova> findByCriadorIdOrderByCriadaEmDesc(UUID criadorId);

    List<Prova> findByPublicadaTrueOrderByCriadaEmDesc();

    List<Prova> findAllByOrderByCriadaEmDesc();
}
