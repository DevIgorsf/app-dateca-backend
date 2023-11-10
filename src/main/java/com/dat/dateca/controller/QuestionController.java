package com.dat.dateca.controller;

import com.dat.dateca.domain.question.QuestionForm;
import com.dat.dateca.domain.question.QuestionMultipleDTO;
import com.dat.dateca.domain.question.QuestionService;
import com.dat.dateca.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/questao")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> createQuestion(HttpServletRequest request, @RequestBody @Valid QuestionForm questionForm) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(questionForm, registrationNumber));
    }

    @GetMapping
    public ResponseEntity<List<QuestionMultipleDTO>> getAllQuestion() {
        return ResponseEntity.ok().body(questionService.getAllQuestion());
    }
}
