package com.dat.dateca.controller;

import com.dat.dateca.domain.course.QuestionForm;
import com.dat.dateca.domain.course.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questao")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody @Valid QuestionForm questionForm) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(questionForm));
    }
}
