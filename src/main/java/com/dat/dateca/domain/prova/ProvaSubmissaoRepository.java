package com.dat.dateca.domain.prova;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProvaSubmissaoRepository extends JpaRepository<ProvaSubmissao, UUID> {

    long countByProva_Id(UUID provaId);

    boolean existsByProva_IdAndStudentId(UUID provaId, UUID studentId);

    List<ProvaSubmissao> findByProva_IdOrderByPontuacaoDescRespondidoEmAsc(UUID provaId);
}
