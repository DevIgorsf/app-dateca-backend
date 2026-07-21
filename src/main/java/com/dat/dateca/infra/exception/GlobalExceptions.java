package com.dat.dateca.infra.exception;

import com.dat.dateca.domain.friendship.BlockedUserException;
import com.dat.dateca.domain.friendship.CannotFriendYourselfException;
import com.dat.dateca.domain.friendship.DuplicateFriendshipException;
import com.dat.dateca.domain.friendship.ForbiddenFriendshipActionException;
import com.dat.dateca.domain.friendship.FriendshipNotFoundException;
import com.dat.dateca.domain.friendship.IllegalFriendshipStateException;
import com.dat.dateca.domain.prova.ProvaAccessDeniedException;
import com.dat.dateca.domain.prova.ProvaConflitoException;
import com.dat.dateca.domain.prova.ProvaInvalidaException;
import com.dat.dateca.exam.domain.ExamNotFoundException;
import com.dat.dateca.importacao.domain.exceptions.CorruptedPdfException;
import com.dat.dateca.importacao.domain.exceptions.ExtractionFailedException;
import com.dat.dateca.importacao.domain.exceptions.ImportJobNotFoundException;
import com.dat.dateca.importacao.domain.exceptions.ImportJobStateConflictException;
import com.dat.dateca.importacao.domain.exceptions.PublishValidationException;
import com.dat.dateca.importacao.domain.exceptions.UnsupportedFileTypeException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptions {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> entityNotFoundHandlerMethod(EntityNotFoundException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), 404, "Not Found", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(se);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), 400, "Bad Request", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(se);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<StandardError> nullPointerException(NullPointerException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Ocorreu uma NullPointerException", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(se);
    }

    @ExceptionHandler(CannotFriendYourselfException.class)
    public ResponseEntity<StandardError> cannotFriendYourselfException(CannotFriendYourselfException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(se);
    }

    @ExceptionHandler(DuplicateFriendshipException.class)
    public ResponseEntity<StandardError> duplicateFriendshipException(DuplicateFriendshipException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(se);
    }

    @ExceptionHandler(IllegalFriendshipStateException.class)
    public ResponseEntity<StandardError> illegalFriendshipStateException(IllegalFriendshipStateException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(se);
    }

    @ExceptionHandler(BlockedUserException.class)
    public ResponseEntity<StandardError> blockedUserException(BlockedUserException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se);
    }

    @ExceptionHandler(ForbiddenFriendshipActionException.class)
    public ResponseEntity<StandardError> forbiddenFriendshipActionException(ForbiddenFriendshipActionException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se);
    }

    @ExceptionHandler(FriendshipNotFoundException.class)
    public ResponseEntity<StandardError> friendshipNotFoundException(FriendshipNotFoundException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(se);
    }

    @ExceptionHandler(ProvaAccessDeniedException.class)
    public ResponseEntity<StandardError> provaAccessDeniedException(ProvaAccessDeniedException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se);
    }

    @ExceptionHandler(ProvaConflitoException.class)
    public ResponseEntity<StandardError> provaConflitoException(ProvaConflitoException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(se);
    }

    @ExceptionHandler(ProvaInvalidaException.class)
    public ResponseEntity<StandardError> provaInvalidaException(ProvaInvalidaException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(se);
    }

    @ExceptionHandler(ImportJobNotFoundException.class)
    public ResponseEntity<StandardError> importJobNotFoundException(ImportJobNotFoundException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(se);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<StandardError> unsupportedFileTypeException(UnsupportedFileTypeException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(se);
    }

    @ExceptionHandler(CorruptedPdfException.class)
    public ResponseEntity<StandardError> corruptedPdfException(CorruptedPdfException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(se);
    }

    @ExceptionHandler(PublishValidationException.class)
    public ResponseEntity<StandardError> publishValidationException(PublishValidationException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(se);
    }

    @ExceptionHandler(ImportJobStateConflictException.class)
    public ResponseEntity<StandardError> importJobStateConflictException(ImportJobStateConflictException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(se);
    }

    @ExceptionHandler(ExtractionFailedException.class)
    public ResponseEntity<StandardError> extractionFailedException(ExtractionFailedException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(se);
    }

    @ExceptionHandler(ExamNotFoundException.class)
    public ResponseEntity<StandardError> examNotFoundException(ExamNotFoundException e, HttpServletRequest request) {
        StandardError se = new StandardError(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(se);
    }
}
