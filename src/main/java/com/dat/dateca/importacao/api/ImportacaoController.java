package com.dat.dateca.importacao.api;

import com.dat.dateca.importacao.application.ImportJobApplicationService;
import com.dat.dateca.importacao.application.dto.ExamDraftReviewView;
import com.dat.dateca.importacao.application.dto.ImageContent;
import com.dat.dateca.importacao.application.dto.ImportJobStatusView;
import com.dat.dateca.importacao.application.dto.PublishResult;
import com.dat.dateca.importacao.application.dto.UpdateExamDraftCommand;
import com.dat.dateca.importacao.application.dto.UploadImportResult;
import com.dat.dateca.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/importacao/provas")
public class ImportacaoController {

    private final ImportJobApplicationService importJobApplicationService;

    public ImportacaoController(ImportJobApplicationService importJobApplicationService) {
        this.importJobApplicationService = importJobApplicationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadImportResult> upload(@RequestParam("file") MultipartFile file) {
        UploadImportResult result = importJobApplicationService.startImport(file, currentUserId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ImportJobStatusView> status(@PathVariable UUID jobId) {
        return ResponseEntity.ok(importJobApplicationService.getStatus(jobId));
    }

    @GetMapping("/{jobId}/rascunho")
    public ResponseEntity<ExamDraftReviewView> draft(@PathVariable UUID jobId) {
        return ResponseEntity.ok(importJobApplicationService.getDraft(jobId));
    }

    @PutMapping("/{jobId}/rascunho")
    public ResponseEntity<ExamDraftReviewView> updateDraft(@PathVariable UUID jobId,
                                                             @RequestBody @Valid UpdateExamDraftCommand command) {
        return ResponseEntity.ok(importJobApplicationService.updateDraft(jobId, command));
    }

    @PostMapping("/{jobId}/publicar")
    public ResponseEntity<PublishResult> publish(@PathVariable UUID jobId) {
        return ResponseEntity.ok(importJobApplicationService.publish(jobId, currentUserId()));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> cancel(@PathVariable UUID jobId) {
        importJobApplicationService.cancel(jobId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{jobId}/rascunho/imagens/{imageId}")
    public ResponseEntity<byte[]> image(@PathVariable UUID jobId, @PathVariable UUID imageId) {
        ImageContent image = importJobApplicationService.getDraftImage(jobId, imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.content());
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((User) principal).getId();
    }
}
