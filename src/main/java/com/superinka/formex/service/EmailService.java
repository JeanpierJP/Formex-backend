package com.superinka.formex.service;

public interface EmailService {

    void sendWelcomeEmail(String to, String name);

    void sendPasswordResetEmail(String to, String token);

    void sendUserConfirmationEmail(String to, String name);

    void sendSupportEmail(
            String fromName,
            String fromEmail,
            String subject,
            String messageText
    );
}
