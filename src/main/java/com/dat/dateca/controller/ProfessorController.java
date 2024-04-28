package com.dat.dateca.controller;

import com.dat.dateca.domain.professor.*;
import com.dat.dateca.domain.student.StudentService;
import com.dat.dateca.domain.student.StudentWithIndex;
import com.dat.dateca.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professor")
public class ProfessorController {

    @Autowired
    ProfessorService professorService;

    @Autowired
    private StudentService studentService;

    @PostMapping
    @Transactional
    public ResponseEntity<ProfessorDTO> createProfessor(@RequestBody @Valid ProfessorCreate professorCreate) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professorService.createProfessor(professorCreate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessorDTO> getProfessor(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.getProfessor(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessorDTO> updateProfessor(@PathVariable Long id, @RequestBody @Valid ProfessorUpdate professorUpdate) {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.updateProfessor(id, professorUpdate));
    }

    @GetMapping
    public ResponseEntity<List<ProfessorDTO>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.getAllProfessors());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteProfessor(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.deleteProfessor(id));
    }

    @GetMapping("/dados")
    public ResponseEntity<Long> getProfessorData() {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.getProfessorData());
    }

    @GetMapping("/perfil")
    public ResponseEntity<ProfessorDTO> getProfile() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        return ResponseEntity.status(HttpStatus.OK).body(professorService.getProfile(registrationNumber));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<StudentWithIndex>> getRanking() {
        return ResponseEntity.status(HttpStatus.OK).body(studentService.rankingAll());
    }

    @PostMapping("/updatePassword")
    @Transactional
    public ResponseEntity<?> updatePassword(HttpServletRequest request, @RequestBody @Valid String newPassword) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        professorService.updatePassword(registrationNumber, newPassword);
        return ResponseEntity.ok().build();
    }
}
