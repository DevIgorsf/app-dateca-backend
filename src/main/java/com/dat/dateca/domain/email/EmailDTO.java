package com.dat.dateca.domain.email;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record EmailDTO (
    String ownerRef,
    String emailFrom,
    String emailTo,
    String subject,
    String text,
    LocalDateTime sendDateEmail,
    StatusEmailEnum statusEmail
) {
    public EmailDTO(Email email) {
        this(
                email.getOwnerRef(),
                email.getEmailFrom(),
                email.getEmailTo(),
                email.getSubject(),
                email.getText(),
                email.getSendDateEmail(),
                email.getStatusEmail());
    }
}
