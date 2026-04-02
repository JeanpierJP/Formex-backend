package com.superinka.formex.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superinka.formex.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private void sendEmail(String to, String subject, String htmlContent) {

        try {
            String url = "https://api.resend.com/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", "Formex <hola@formex.digital>");
            body.put("to", new String[]{to});
            body.put("subject", subject);
            body.put("html", htmlContent);

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(body);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            System.out.println("✅ Email enviado correctamente con Resend");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error enviando email con Resend");
        }
    }

    // ===============================
    // WELCOME EMAIL
    // ===============================

    @Async
    @Override
    public void sendWelcomeEmail(String to, String name) {

        String html = """
            <div style='font-family: Arial, sans-serif;'>
                <h1 style='color:#FF5722;'>¡Hola, %s!</h1>
                <p>Tu cuenta en <strong>Formex</strong> fue creada exitosamente.</p>
                <a href='http://localhost:5173//login'
                   style='background:#FF5722;color:white;padding:10px 20px;
                          text-decoration:none;border-radius:5px;'>
                   Ir a la plataforma
                </a>
                <br/><br/>
                <p>El equipo de Formex</p>
            </div>
        """.formatted(name);

        sendEmail(to, "¡Bienvenido a FORMEX!", html);
    }

    // ===============================
    // PASSWORD RESET
    // ===============================

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {

        String resetUrl = "http://localhost:5173/reset-password?token=" + token;

        String html = """
            <div style='font-family: Arial;'>
                <h2 style='color:#FF5722;'>Recupera tu contraseña</h2>
                <p>Haz clic en el botón para restablecer tu contraseña:</p>
                <a href='%s'
                   style='background:#FF5722;color:white;padding:10px 20px;
                          text-decoration:none;border-radius:5px;'>
                   Restablecer contraseña
                </a>
                <p>Si no solicitaste esto, ignora este correo.</p>
            </div>
        """.formatted(resetUrl);

        sendEmail(to, "Recuperación de contraseña - FORMEX", html);
    }

    // ===============================
    // SOPORTE (recibe hola@formex.digital)
    // ===============================

    @Override
    public void sendSupportEmail(
            String fromName,
            String fromEmail,
            String subject,
            String messageText
    ) {

        String html = """
            <div style='font-family: Arial;'>
                <h2>Nuevo mensaje de soporte</h2>
                <p><b>Nombre:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>Mensaje:</b></p>
                <p>%s</p>
            </div>
        """.formatted(fromName, fromEmail, messageText);

        sendEmail("hola@formex.digital",
                "Soporte FORMEX - " + subject,
                html);
    }

    // ===============================
    // CONFIRMACION AL USUARIO
    // ===============================

    @Async
    @Override
    public void sendUserConfirmationEmail(String to, String name) {

        String html = """
            <div style='font-family: Arial;'>
                <h2>Hola, %s</h2>
                <p>Hemos recibido tu mensaje.</p>
                <p>Nuestro equipo te responderá pronto.</p>
                <br/>
                <p>Gracias por confiar en Formex.</p>
            </div>
        """.formatted(name);

        sendEmail(to,
                "Recibimos tu mensaje - FORMEX",
                html);
    }
}