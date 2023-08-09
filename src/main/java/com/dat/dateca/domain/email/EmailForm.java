package com.dat.dateca.domain.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailForm(
    @NotBlank
    String ownerRef,
    @NotBlank
    @Email
    String emailFrom,
    @NotBlank
    @Email
    String emailTo,
    @NotBlank
    String subject,
    @NotBlank
    String text) { }
