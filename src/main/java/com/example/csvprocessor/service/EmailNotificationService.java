package com.example.csvprocessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificationService(JavaMailSender mailSender, org.springframework.core.env.Environment environment) {
        this.mailSender = mailSender;
        this.fromAddress = environment.getProperty("app.email-from", "no-reply@example.com");
    }

    public void sendNotification(String recipient, String zipCode, String state) {
        if (recipient == null || recipient.isBlank()) {
            log.warn("Skipping email notification because recipient address is empty for zip code {}", zipCode);
            return;
        }

        try {
            var message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipient);
            message.setSubject("ZIP Code Processing Result");
            message.setText(buildMessageBody(zipCode, state));
            mailSender.send(message);
            log.info("Email notification sent to {} for zip code {}", recipient, zipCode);
            System.out.println("************Sent email to: " + recipient + " for zip code: " + zipCode);    
        } catch (MailException ex) {
            log.warn("Failed to send email to {} for zip code {}: {}", recipient, zipCode, ex.getMessage());
        }
    }

    private String buildMessageBody(String zipCode, String state) {
        return "Your ZIP code record has been processed." + System.lineSeparator() +
                "ZIP code: " + zipCode + System.lineSeparator() +
                "Resolved state: " + state + System.lineSeparator() +
                System.lineSeparator() +
                "Thank you for using the ZIP code processor.";
    }
}
