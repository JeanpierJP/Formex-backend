package com.superinka.formex.controller;

import com.superinka.formex.model.SupportMessageDTO;
import com.superinka.formex.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://www.formex.digital"
})
public class SupportController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<String> sendSupportMessage(
            @Valid @RequestBody SupportMessageDTO dto
    ) {
        // Enviar al soporte
        emailService.sendSupportEmail(
                dto.getName(),
                dto.getEmail(),
                dto.getSubject(),
                dto.getMessage()
        );

        // Enviar confirmación al usuario
        emailService.sendUserConfirmationEmail(
                dto.getEmail(),
                dto.getName()
        );

        return ResponseEntity.ok("Mensaje enviado correctamente. Revisa tu correo de confirmación.");
    }

}


