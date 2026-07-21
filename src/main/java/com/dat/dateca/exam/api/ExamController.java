package com.dat.dateca.exam.api;

import com.dat.dateca.exam.application.ExamQueryService;
import com.dat.dateca.exam.application.dto.ExamDetailView;
import com.dat.dateca.exam.application.dto.ExamSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/exames")
public class ExamController {

    private final ExamQueryService examQueryService;

    public ExamController(ExamQueryService examQueryService) {
        this.examQueryService = examQueryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamDetailView> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(examQueryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ExamSummaryView>> list(Pageable pageable) {
        return ResponseEntity.ok(examQueryService.list(pageable));
    }
}
