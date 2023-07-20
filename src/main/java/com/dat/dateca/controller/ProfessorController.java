package com.dat.dateca.controller;

import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorCreate;
import com.dat.dateca.domain.professor.ProfessorCreateDTO;
import com.dat.dateca.domain.professor.ProfessorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professor")
public class ProfessorController {

    @Autowired
    ProfessorService professorService;

    @PostMapping
    @Transactional
    public ResponseEntity<ProfessorCreateDTO> createProfessor(@RequestBody @Valid ProfessorCreate professorCreate) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professorService.createProfessor(professorCreate));
    }

    @GetMapping
    public ResponseEntity<List<Professor>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.getAll());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteProfessor(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.deleteProfessor(id));
    }
}
