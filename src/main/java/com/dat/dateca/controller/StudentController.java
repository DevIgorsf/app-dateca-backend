package com.dat.dateca.controller;

import com.dat.dateca.domain.professor.ProfessorCreate;
import com.dat.dateca.domain.professor.ProfessorDTO;
import com.dat.dateca.domain.student.*;
import com.dat.dateca.domain.user.User;
import com.dat.dateca.domain.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aluno")
public class StudentController {

    @Autowired
    private UserService userService;
    @Autowired
    private StudentService studentService;

    @PostMapping("/cadastrar")
    @Transactional
    public ResponseEntity<StudentDTO> cadastroAluno(@RequestBody @Valid StudentCadastro studentCadastro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.cadastroAluno(studentCadastro));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<StudentWithIndex>> rankingStudent(HttpServletRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.rankingStudent(registrationNumber));
    }

    @GetMapping("/ranking-geral")
    public ResponseEntity<List<StudentWithIndex>> rankingAll() {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.rankingAll());
    }

    @GetMapping("/perfil")
    public ResponseEntity<Student> getPerfil() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        return ResponseEntity.status(HttpStatus.OK).body(studentService.getPerfil(registrationNumber));
    }

    @PutMapping("/perfil")
    public ResponseEntity<StudentDTO> updatePerfil(@RequestBody @Valid StudentCadastro studentCadastro) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.updatePerfil(registrationNumber, studentCadastro));
    }

}
