package com.dat.dateca.controller;

import com.dat.dateca.domain.ranking.RankingResponseDTO;
import com.dat.dateca.domain.ranking.RankingService;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/ranking/friends")
    public ResponseEntity<RankingResponseDTO> friendsRanking(@PageableDefault(size = 20) Pageable pageable) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(rankingService.getFriendsRanking(current, pageable));
    }
}
