package com.dat.dateca.controller;

import com.dat.dateca.domain.question.*;
import com.dat.dateca.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questao")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private ImageQuestionRepository imageQuestionRepository;

    @PostMapping
    public ResponseEntity<QuestionMultipleDTO> createQuestion(@RequestBody @Valid QuestionForm questionForm) {
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

    @PostMapping("/answerQuestion/{id}")
    @Transactional
    public ResponseEntity<QuestionAnswerDTO> answerQuestion(HttpServletRequest request,@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        String correctAnswer = requestBody.get("correctAnswer");
        return ResponseEntity.status(HttpStatus.OK).body(questionService.answerQuestion(id, correctAnswer, registrationNumber));
    }

    @GetMapping("/dados")
    public ResponseEntity<Long> getQuestionData() {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestionData());
    }

    @GetMapping("/resultados")
    public ResponseEntity<QuestionResultDTO> getQuestionResult() {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestionResult());
    }

    @PostMapping("/imagens")
    public ResponseEntity<QuestionMultipleChoice> salvar(
            @RequestParam("imageFile") MultipartFile[] files,
            @RequestParam("statement") String statement,
            @RequestParam("pointsEnum") String pointsEnum,
            @RequestParam("course") Long course,
            @RequestParam("correctAnswer") Character correctAnswer,
            @RequestParam("alternativeA") String alternativeA,
            @RequestParam("alternativeB") String alternativeB,
            @RequestParam("alternativeC") String alternativeC,
            @RequestParam("alternativeD") String alternativeD,
            @RequestParam("alternativeE") String alternativeE) {
        try {

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String registrationNumber = ((User)principal).getLogin();

            return ResponseEntity.ok(questionService.salvarImagens(
                    registrationNumber,
                    files,
                    statement,
                    pointsEnum,
                    course,
                    correctAnswer,
                    alternativeA,
                    alternativeB,
                    alternativeC,
                    alternativeD,
                    alternativeE));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/imagens/{id}")
    public ResponseEntity<QuestionMultipleAllDTO> updated(
            @RequestParam("imageFile") MultipartFile[] files,
            @RequestParam("statement") String statement,
            @RequestParam("pointsEnum") String pointsEnum,
            @RequestParam("course") Long course,
            @RequestParam("correctAnswer") Character correctAnswer,
            @RequestParam("alternativeA") String alternativeA,
            @RequestParam("alternativeB") String alternativeB,
            @RequestParam("alternativeC") String alternativeC,
            @RequestParam("alternativeD") String alternativeD,
            @RequestParam("alternativeE") String alternativeE,
            long id) {
        try {

            return ResponseEntity.ok(questionService.updateImagens(
                    files,
                    statement,
                    pointsEnum,
                    course,
                    correctAnswer,
                    alternativeA,
                    alternativeB,
                    alternativeC,
                    alternativeD,
                    alternativeE,
                    id));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
