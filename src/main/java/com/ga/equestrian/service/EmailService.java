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
     * Sends the email verification code to a new user.
     *
     * @param to the recipient's email address.
     * @param code the verification code the user must enter.
     */
    public void sendVerificationCode(String to, String code){
        String subject = "Verify your email";
        String text = "Welcome to the Equestrian Centre!\n\n"
                + "Use the code below to verify your email address:\n"
                + code + "\n\n"
                + "This code expires in 15 minutes.";
        sendEmail(to, subject, text);
    }

    /**
     * Sends the password reset code to a user who forgot their password.
     *
     * @param to the recipient's email address.
     * @param code the reset code the user must enter.
     */
    public void sendPasswordResetCode(String to, String code){
        String subject = "Reset your password";
        String text = "We received a request to reset your Equestrian Centre password.\n\n"
                + "Use the code below to set a new password:\n"
                + code + "\n\n"
                + "This code expires in 15 minutes.\n";
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
