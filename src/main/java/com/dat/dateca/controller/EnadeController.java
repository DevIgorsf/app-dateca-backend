package com.dat.dateca.controller;

import com.dat.dateca.domain.enade.*;
import com.dat.dateca.domain.question.QuestionAnswerDTO;
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
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enade")
public class EnadeController {

    @Autowired
    private EnadeService enadeService;

    @Autowired
    private ImageEnadeRepository imageEnadeRepository;

    @PostMapping
    public ResponseEntity<EnadeDTO> createEnade(@RequestBody @Valid EnadeForm enadeForm) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enadeService.createEnade(enadeForm));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnadeDTO> updateEnade(@PathVariable Long id, @RequestBody @Valid EnadeForm EnadeForm) {
        return ResponseEntity.status(HttpStatus.OK).body(enadeService.updateEnade(id, EnadeForm));
    }

//    @GetMapping("/images")
//    public ResponseEntity<List<EnadeAllDTO>> getAllEnade() {
//        return ResponseEntity.ok().body(enadeService.getAllEnade());
//    }
//
//    @GetMapping
//    public ResponseEntity<List<EnadeAllDTO>> getAllEnadeWithoutImages() {
//        return ResponseEntity.ok().body(enadeService.getAllEnadeWithoutImages());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<EnadeDTO> getEnade(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(enadeService.getEnade(id));
    }

    @GetMapping("/aluno")
    public ResponseEntity<EnadeRandDTO> getEnadeAleatoria() {
        return ResponseEntity.status(HttpStatus.OK).body(enadeService.getEnadeAleatoria());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEnade(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(enadeService.deleteEnade(id));
    }

    @PostMapping("/answerEnade/{id}")
    @Transactional
    public ResponseEntity<QuestionAnswerDTO> answerEnade(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String registrationNumber = ((User)principal).getLogin();
        String correctAnswer = requestBody.get("correctAnswer");
        return ResponseEntity.status(HttpStatus.OK).body(enadeService.answerEnade(id, correctAnswer, registrationNumber));
    }

    @GetMapping("/dados")
    public ResponseEntity<Long> getEnadeData() {
        return ResponseEntity.status(HttpStatus.OK).body(enadeService.getEnadeData());
    }

    @PostMapping("/imagens")
    public ResponseEntity<Enade> salvar(
            @RequestParam("imageFile") MultipartFile[] files,
            @RequestParam("ano") Year ano,
            @RequestParam("number") int number,
            @RequestParam("statement") String statement,
            @RequestParam("pointsEnum") String pointsEnum,
            @RequestParam("correctAnswer") Character correctAnswer,
            @RequestParam("alternativeA") String alternativeA,
            @RequestParam("alternativeB") String alternativeB,
            @RequestParam("alternativeC") String alternativeC,
            @RequestParam("alternativeD") String alternativeD,
            @RequestParam("alternativeE") String alternativeE) {


        return ResponseEntity.ok(enadeService.salvarImagens(
                files,
                ano,
                number,
                statement,
                pointsEnum,
                correctAnswer,
                alternativeA,
                alternativeB,
                alternativeC,
                alternativeD,
                alternativeE));
    }

    @GetMapping("/imagens/{id}")
    public ResponseEntity<List<ImageEnade>> getImages(@PathVariable Long id) {
        return ResponseEntity.ok(enadeService.getImages(id));
    }
}
