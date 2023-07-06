package com.dat.dateca.controller;

import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorCreate;
import com.dat.dateca.domain.professor.ProfessorCreateDTO;
import com.dat.dateca.domain.professor.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/professor")
public class ProfessorController {

    @Autowired
    ProfessorService professorService;

    @PostMapping
    public ResponseEntity<ProfessorCreateDTO> createProfessor (@RequestBody ProfessorCreate professorCreate) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professorService.createProfessor(professorCreate));
    }
}
