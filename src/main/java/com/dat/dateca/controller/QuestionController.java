package com.dat.dateca.controller;

import com.dat.dateca.domain.question.*;
import com.dat.dateca.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questao")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionMultipleChoice> createQuestion(HttpServletRequest request, @RequestBody @Valid QuestionForm questionForm) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(questionForm, registrationNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionMultipleDTO> updateQuestion(@PathVariable Long id, @RequestBody @Valid QuestionForm questionForm) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.updateQuestion(id, questionForm));
    }

    @GetMapping
    public ResponseEntity<List<QuestionMultipleAllDTO>> getAllQuestion() {
        return ResponseEntity.ok().body(questionService.getAllQuestion());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionMultipleDTO> getQuestion(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestion(id));
    }

    @GetMapping("/aluno")
    public ResponseEntity<QuestionMultipleChoiceRandDTO> getQuestionAleatoria() {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestionAleatoria());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(questionService.deleteQuestion(id));
    }

//    @PostMapping("/answerQuestion/{id}")
//    public ResponseEntity<?> answerQuestion(HttpServletRequest request,@PathVariable Long id, @RequestBody @Valid QuestionForm questionForm) {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String registrationNumber = ((User)principal).getLogin();
//        return ResponseEntity.status(HttpStatus.OK).body(questionService.answerQuestion(questionForm, registrationNumber));
//    }
}
