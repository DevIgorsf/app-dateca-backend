package com.dat.dateca.domain.prova;

import com.dat.dateca.domain.question.PointsEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Prova {

    @Id
    @UuidGenerator
    private UUID id;

    private String titulo;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String disciplina;

    @Enumerated(EnumType.STRING)
    private PointsEnum dificuldade;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String capaUrl;

    private LocalDate dataAbertura;

    private LocalTime horaAbertura;

    private LocalDate dataEncerramento;

    private Integer maxParticipantes;

    @Enumerated(EnumType.STRING)
    private VisibilidadeProva visibilidade;

    private boolean publicada;

    @Column(name = "criador_id", nullable = false)
    private UUID criadorId;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @OneToMany(mappedBy = "prova", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<ProvaQuestao> questoes = new ArrayList<>();

    public Prova(ProvaForm form, UUID criadorId, boolean publicada) {
        this.titulo = form.titulo();
        this.descricao = form.descricao();
        this.disciplina = form.disciplina();
        this.dificuldade = form.dificuldade();
        this.capaUrl = form.capaUrl();
        this.dataAbertura = form.dataAbertura();
        this.horaAbertura = form.horaAbertura();
        this.dataEncerramento = form.dataEncerramento();
        this.maxParticipantes = form.maxParticipantes();
        this.visibilidade = form.visibilidade();
        this.publicada = publicada;
        this.criadorId = criadorId;
        this.criadaEm = LocalDateTime.now();
        this.questoes = montarQuestoes(form);
    }

    public void atualizar(ProvaForm form, boolean publicada) {
        this.titulo = form.titulo();
        this.descricao = form.descricao();
        this.disciplina = form.disciplina();
        this.dificuldade = form.dificuldade();
        this.capaUrl = form.capaUrl();
        this.dataAbertura = form.dataAbertura();
        this.horaAbertura = form.horaAbertura();
        this.dataEncerramento = form.dataEncerramento();
        this.maxParticipantes = form.maxParticipantes();
        this.visibilidade = form.visibilidade();
        this.publicada = publicada;

        this.questoes.clear();
        this.questoes.addAll(montarQuestoes(form));
    }

    private List<ProvaQuestao> montarQuestoes(ProvaForm form) {
        List<ProvaQuestao> novasQuestoes = new ArrayList<>();
        List<ProvaQuestaoForm> questoesForm = form.questoes() != null ? form.questoes() : List.of();

        int ordem = 0;
        for (ProvaQuestaoForm questaoForm : questoesForm) {
            novasQuestoes.add(new ProvaQuestao(this, questaoForm, ordem++));
        }

        return novasQuestoes;
    }

    public void publicar() {
        this.publicada = true;
    }

    public void definirCapa(String capaUrl) {
        this.capaUrl = capaUrl;
    }

    public String getStatusCalculado() {
        if (!publicada) {
            return "Rascunho";
        }

        LocalDate hoje = LocalDate.now();

        if (dataAbertura != null && dataAbertura.isAfter(hoje)) {
            return "Agendado";
        }

        if (dataEncerramento != null && dataEncerramento.isBefore(hoje)) {
            return "Encerrado";
        }

        return "Ativo";
    }

    public boolean isRankingDisponivel() {
        String status = getStatusCalculado();
        return status.equals("Ativo") || status.equals("Encerrado");
    }
}
