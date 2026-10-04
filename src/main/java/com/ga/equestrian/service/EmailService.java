package com.ga.equestrian.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service responsible for sending emails to the user like: verification.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;


    /**
     * Sends the email verification link to a new user.
     *
     * @param to the recipient's email address.
     * @param link the verification link the user must open.
     */
    public void sendVerificationEmail(String to, String link){
        String subject = "Verify your email";
        String text = "Welcome to the Equestrian Centre!\n\n"
                + "Please open the link below to verify your email address:\n"
                + link + "\n\n"
                + "This link expires in 1 hour.";
        sendEmail(to, subject, text);
    }

    /**
     * Builds and sends a plain-text email.
     *
     * @param to    the recipient's email address
     * @param subject the subject line
     * @param text    the body of the email
     */
    private void sendEmail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try{
            mailSender.send(message);
        }catch (MailException ex){
            log.error("Could not send email to {}: {}", to, ex.getMessage());
        }
    }

}
