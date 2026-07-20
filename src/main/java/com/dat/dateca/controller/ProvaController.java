package com.dat.dateca.controller;

import com.dat.dateca.domain.prova.ProvaAlunoDTO;
import com.dat.dateca.domain.prova.ProvaCapaDTO;
import com.dat.dateca.domain.prova.ProvaDetalheDTO;
import com.dat.dateca.domain.prova.ProvaForm;
import com.dat.dateca.domain.prova.ProvaRankingDTO;
import com.dat.dateca.domain.prova.ProvaResponderForm;
import com.dat.dateca.domain.prova.ProvaResultadoDTO;
import com.dat.dateca.domain.prova.ProvaResumoDTO;
import com.dat.dateca.domain.prova.ProvaService;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/prova")
public class ProvaController {

    @Autowired
    private ProvaService provaService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/minhas")
    public ResponseEntity<List<ProvaResumoDTO>> listarMinhas() {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.listarMinhas(current));
    }

    @GetMapping("/publicas")
    public ResponseEntity<List<ProvaResumoDTO>> listarPublicas() {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.listarPublicas(current));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvaDetalheDTO> buscarDetalhe(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.buscarDetalhe(id, current));
    }

    @PostMapping
    public ResponseEntity<ProvaDetalheDTO> criar(@RequestBody @Valid ProvaForm form) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.status(HttpStatus.CREATED).body(provaService.criar(form, current));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProvaDetalheDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid ProvaForm form) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.atualizar(id, form, current));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        provaService.excluir(id, current);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<ProvaDetalheDTO> publicar(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.publicar(id, current));
    }

    @PostMapping("/{id}/capa")
    public ResponseEntity<ProvaCapaDTO> salvarCapa(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        Student current = studentService.getAuthenticatedStudent();
        try {
            return ResponseEntity.ok(provaService.salvarCapa(id, file, current));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{id}/questoes/aluno")
    public ResponseEntity<ProvaAlunoDTO> buscarParaAluno(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.buscarParaAluno(id, current));
    }

    @PostMapping("/{id}/responder")
    public ResponseEntity<ProvaResultadoDTO> responder(@PathVariable UUID id, @RequestBody @Valid ProvaResponderForm form) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.responder(id, form, current));
    }

    @GetMapping("/{id}/ranking")
    public ResponseEntity<List<ProvaRankingDTO>> ranking(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(provaService.ranking(id, current));
    }
}
