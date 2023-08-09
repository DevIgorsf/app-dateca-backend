package com.dat.dateca.domain.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    @Autowired
    EmailRepository emailRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    public EmailDTO sendEmail(EmailForm emailForm) {

        Email email = new Email(emailForm);
        email.setSendDateEmail(LocalDateTime.now());

        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

            simpleMailMessage.setFrom(email.getEmailFrom());
            simpleMailMessage.setTo(email.getEmailTo());
            simpleMailMessage.setSubject(email.getSubject());
            simpleMailMessage.setText(email.getText());

            javaMailSender.send(simpleMailMessage);

            email.setStatusEmail(StatusEmailEnum.SEND);
        } catch (MailException mailException) {
            email.setStatusEmail(StatusEmailEnum.ERROR);
        } finally {
            emailRepository.save(email);
            return new EmailDTO(email);
        }

    }

}
