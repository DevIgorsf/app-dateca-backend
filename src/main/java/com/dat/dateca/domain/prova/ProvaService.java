package com.dat.dateca.domain.prova;

import com.dat.dateca.domain.friendship.FriendshipRepository;
import com.dat.dateca.domain.friendship.FriendshipStatus;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProvaService {

    private static final long TAMANHO_MAXIMO_CAPA = 5 * 1024 * 1024;

    @Autowired
    private ProvaRepository provaRepository;

    @Autowired
    private ProvaSubmissaoRepository provaSubmissaoRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    public List<ProvaResumoDTO> listarMinhas(Student current) {
        return provaRepository.findByCriadorIdOrderByCriadaEmDesc(current.getId()).stream()
                .map(prova -> new ProvaResumoDTO(prova, provaSubmissaoRepository.countByProva_Id(prova.getId())))
                .toList();
    }

    public List<ProvaResumoDTO> listarPublicas(Student current) {
        return provaRepository.findByPublicadaTrueOrderByCriadaEmDesc().stream()
                .filter(prova -> !prova.getCriadorId().equals(current.getId()))
                .filter(prova -> prova.getVisibilidade() != VisibilidadeProva.AMIGOS
                        || isFriend(prova.getCriadorId(), current.getId()))
                .map(prova -> new ProvaResumoDTO(prova, provaSubmissaoRepository.countByProva_Id(prova.getId())))
                .toList();
    }

    public ProvaDetalheDTO buscarDetalhe(UUID id, Student current) {
        Prova prova = getProvaOrThrow(id);
        checkVisualizacao(prova, current);
        return new ProvaDetalheDTO(prova, provaSubmissaoRepository.countByProva_Id(id));
    }

    @Transactional(readOnly = true)
    public List<ProvaAdminResumoDTO> listarTodas() {
        List<Prova> provas = provaRepository.findAllByOrderByCriadaEmDesc();

        Set<UUID> criadorIds = provas.stream().map(Prova::getCriadorId).collect(Collectors.toSet());
        Map<UUID, String> nomesPorCriador = studentRepository.findAllById(criadorIds).stream()
                .collect(Collectors.toMap(Student::getId, Student::getName));

        return provas.stream()
                .map(prova -> new ProvaAdminResumoDTO(
                        prova,
                        provaSubmissaoRepository.countByProva_Id(prova.getId()),
                        nomesPorCriador.getOrDefault(prova.getCriadorId(), "Desconhecido")))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProvaAdminDetalheDTO buscarDetalheAdmin(UUID id) {
        Prova prova = getProvaOrThrow(id);
        String criadorNome = studentRepository.findById(prova.getCriadorId())
                .map(Student::getName)
                .orElse("Desconhecido");
        return new ProvaAdminDetalheDTO(prova, provaSubmissaoRepository.countByProva_Id(id), criadorNome);
    }

    @Transactional
    public ProvaDetalheDTO criar(ProvaForm form, Student current) {
        boolean publicar = deveSerPublicada(form);
        validarQuestoesParaPublicacao(form.questoes(), publicar);

        Prova prova = new Prova(form, current.getId(), publicar);
        provaRepository.save(prova);

        return new ProvaDetalheDTO(prova, 0);
    }

    @Transactional
    public ProvaDetalheDTO atualizar(UUID id, ProvaForm form, Student current) {
        Prova prova = getProvaOrThrow(id);
        checkEdicao(prova, current);

        boolean publicar = deveSerPublicada(form);
        validarQuestoesParaPublicacao(form.questoes(), publicar);

        prova.atualizar(form, publicar);
        provaRepository.save(prova);

        return new ProvaDetalheDTO(prova, provaSubmissaoRepository.countByProva_Id(id));
    }

    @Transactional
    public void excluir(UUID id, Student current) {
        Prova prova = getProvaOrThrow(id);
        checkEdicao(prova, current);
        provaRepository.delete(prova);
    }

    @Transactional
    public ProvaDetalheDTO publicar(UUID id, Student current) {
        Prova prova = getProvaOrThrow(id);
        checkEdicao(prova, current);

        if (prova.isPublicada()) {
            throw new ProvaConflitoException("Esta prova já está publicada");
        }

        if (prova.getQuestoes().isEmpty()) {
            throw new ProvaInvalidaException("Uma prova publicada precisa ter ao menos uma questão");
        }

        prova.publicar();
        provaRepository.save(prova);

        return new ProvaDetalheDTO(prova, provaSubmissaoRepository.countByProva_Id(id));
    }

    @Transactional
    public ProvaCapaDTO salvarCapa(UUID id, MultipartFile file, Student current) throws IOException {
        Prova prova = getProvaOrThrow(id);
        checkEdicao(prova, current);

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
            throw new ProvaInvalidaException("A capa deve ser uma imagem PNG ou JPG");
        }

        if (file.getSize() > TAMANHO_MAXIMO_CAPA) {
            throw new ProvaInvalidaException("A capa deve ter no máximo 5MB");
        }

        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        String capaUrl = "data:" + contentType + ";base64," + base64;

        prova.definirCapa(capaUrl);
        provaRepository.save(prova);

        return new ProvaCapaDTO(capaUrl);
    }

    public ProvaAlunoDTO buscarParaAluno(UUID id, Student current) {
        Prova prova = getProvaOrThrow(id);
        checkVisualizacao(prova, current);

        if (!"Ativo".equals(prova.getStatusCalculado())) {
            throw new ProvaConflitoException("Esta prova não está disponível para respostas no momento");
        }

        return new ProvaAlunoDTO(prova);
    }

    @Transactional
    public ProvaResultadoDTO responder(UUID id, ProvaResponderForm form, Student current) {
        Prova prova = getProvaOrThrow(id);
        checkVisualizacao(prova, current);

        if (!"Ativo".equals(prova.getStatusCalculado())) {
            throw new ProvaConflitoException("Esta prova não está ativa para respostas no momento");
        }

        if (provaSubmissaoRepository.existsByProva_IdAndStudentId(id, current.getId())) {
            throw new ProvaConflitoException("Você já respondeu esta prova");
        }

        if (prova.getMaxParticipantes() != null
                && provaSubmissaoRepository.countByProva_Id(id) >= prova.getMaxParticipantes()) {
            throw new ProvaConflitoException("O limite de participantes desta prova foi atingido");
        }

        Map<UUID, Character> respostasPorQuestao = new HashMap<>();
        for (ProvaRespostaForm resposta : form.respostas()) {
            respostasPorQuestao.put(resposta.provaQuestaoId(), resposta.respostaEscolhida());
        }

        List<ProvaResposta> respostasSalvas = new ArrayList<>();
        List<ProvaResultadoItemDTO> gabarito = new ArrayList<>();
        int acertos = 0;

        for (ProvaQuestao questao : prova.getQuestoes()) {
            Character escolhida = respostasPorQuestao.get(questao.getId());
            boolean correta = escolhida != null
                    && questao.getCorrectAnswer() != null
                    && Character.toUpperCase(questao.getCorrectAnswer()) == Character.toUpperCase(escolhida);

            if (correta) {
                acertos++;
            }

            if (escolhida != null) {
                respostasSalvas.add(new ProvaResposta(questao.getId(), escolhida));
            }

            gabarito.add(new ProvaResultadoItemDTO(
                    questao.getId(), questao.getCorrectAnswer(), escolhida, correta, questao.getComment()));
        }

        int pontuacao = acertos * prova.getDificuldade().getKey();

        ProvaSubmissao submissao = new ProvaSubmissao(prova, current.getId(), respostasSalvas, pontuacao, acertos);
        provaSubmissaoRepository.save(submissao);

        return new ProvaResultadoDTO(pontuacao, prova.getQuestoes().size(), acertos, gabarito);
    }

    public List<ProvaRankingDTO> ranking(UUID id, Student current) {
        Prova prova = getProvaOrThrow(id);
        checkVisualizacao(prova, current);

        if (!prova.isRankingDisponivel()) {
            throw new ProvaConflitoException("O ranking desta prova ainda não está disponível");
        }

        List<ProvaSubmissao> submissoes = provaSubmissaoRepository.findByProva_IdOrderByAcertosDescRespondidoEmAsc(id);

        Set<UUID> studentIds = submissoes.stream().map(ProvaSubmissao::getStudentId).collect(Collectors.toSet());
        Map<UUID, Student> students = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<ProvaRankingDTO> resultado = new ArrayList<>();
        int posicao = 1;
        for (ProvaSubmissao submissao : submissoes) {
            Student student = students.get(submissao.getStudentId());
            resultado.add(new ProvaRankingDTO(
                    posicao++,
                    submissao.getStudentId(),
                    student != null ? student.getName() : "Desconhecido",
                    submissao.getPontuacao(),
                    submissao.getAcertos(),
                    submissao.getRespondidoEm()
            ));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<ProvaRankingDTO> rankingAdmin(UUID id) {
        getProvaOrThrow(id);

        List<ProvaSubmissao> submissoes =
                provaSubmissaoRepository.findByProva_IdOrderByAcertosDescRespondidoEmAsc(id);

        Set<UUID> studentIds = submissoes.stream().map(ProvaSubmissao::getStudentId).collect(Collectors.toSet());
        Map<UUID, Student> students = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<ProvaRankingDTO> resultado = new ArrayList<>();
        int posicao = 1;
        for (ProvaSubmissao submissao : submissoes) {
            Student student = students.get(submissao.getStudentId());
            resultado.add(new ProvaRankingDTO(
                    posicao++,
                    submissao.getStudentId(),
                    student != null ? student.getName() : "Desconhecido",
                    submissao.getPontuacao(),
                    submissao.getAcertos(),
                    submissao.getRespondidoEm()
            ));
        }

        return resultado;
    }

    private boolean isFriend(UUID a, UUID b) {
        return friendshipRepository.findBetween(a, b)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    private void checkVisualizacao(Prova prova, Student current) {
        UUID currentId = current.getId();

        if (prova.getCriadorId().equals(currentId)) {
            return;
        }

        if (!prova.isPublicada()) {
            throw new ProvaAccessDeniedException();
        }

        if (prova.getVisibilidade() == VisibilidadeProva.AMIGOS && !isFriend(prova.getCriadorId(), currentId)) {
            throw new ProvaAccessDeniedException();
        }
    }

    private void checkEdicao(Prova prova, Student current) {
        if (!prova.getCriadorId().equals(current.getId())) {
            throw new ProvaAccessDeniedException();
        }
    }

    private boolean deveSerPublicada(ProvaForm form) {
        return form.status() == null || !"Rascunho".equalsIgnoreCase(form.status());
    }

    private void validarQuestoesParaPublicacao(List<ProvaQuestaoForm> questoes, boolean publicar) {
        if (publicar && (questoes == null || questoes.isEmpty())) {
            throw new ProvaInvalidaException("Uma prova publicada precisa ter ao menos uma questão");
        }
    }

    private Prova getProvaOrThrow(UUID id) {
        return provaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prova não encontrada: " + id));
    }
}
