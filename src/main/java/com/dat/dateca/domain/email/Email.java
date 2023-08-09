package com.dat.dateca.domain.email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Email implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "char(36)")
    private UUID id;

    private String ownerRef;

    private String emailFrom;

    private String emailTo;

    private String subject;

    @Column(columnDefinition = "TEXT") // Pode receber mais que 255 caracteres
    private String text;

    private LocalDateTime sendDateEmail;

    private StatusEmailEnum statusEmail;

    public Email(EmailForm emailForm) {
        this.ownerRef = emailForm.ownerRef();
        this.emailFrom = emailForm.emailFrom();
        this.emailTo = emailForm.emailTo();
        this.subject = emailForm.subject();
        this.text = emailForm.text();
    }
}
