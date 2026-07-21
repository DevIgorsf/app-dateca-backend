package com.dat.dateca.exam.domain;

public class ExamNotFoundException extends RuntimeException {
    public ExamNotFoundException(String message) {
        super(message);
    }
}
