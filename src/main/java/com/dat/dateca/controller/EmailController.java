package com.dat.dateca.controller;

import com.dat.dateca.domain.email.EmailDTO;
import com.dat.dateca.domain.email.EmailForm;
import com.dat.dateca.domain.email.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping
    public ResponseEntity<EmailDTO> sendEmail(@RequestBody @Valid EmailForm emailForm) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailService.sendEmail(emailForm));
    }

}
