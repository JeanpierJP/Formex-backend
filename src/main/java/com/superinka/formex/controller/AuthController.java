package com.superinka.formex.controller;

import com.superinka.formex.model.PasswordResetToken;
import com.superinka.formex.model.Role;
import com.superinka.formex.model.User;
import com.superinka.formex.model.enums.RoleName;
import com.superinka.formex.payload.request.LoginRequest;
import com.superinka.formex.payload.request.SignupRequest;
import com.superinka.formex.payload.response.MessageResponse;
import com.superinka.formex.repository.PasswordResetTokenRepository;
import com.superinka.formex.repository.RoleRepository;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.Auth0Service;
import com.superinka.formex.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;
    private final Auth0Service auth0Service;

    /**
     * NOTA: El login ahora se maneja vía Auth0 OAuth2
     * Este endpoint está deshabilitado. Los usuarios deben usar Auth0 Universal Login.
     * Para testing local, usar Auth0 Management API o el endpoint de signup.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                new MessageResponse("Login debe realizarse a través de Auth0 OAuth2. Por favor usa Auth0 Universal Login.")
        );
    }

    /**
     * Registro de usuario nuevo (sincroniza con Auth0 y BD local)
     * POST /auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Validar que el email no esté registrado localmente
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: El email ya está registrado!"));
        }

        try {
            // 1. Separar nombre completo en nombre y apellido
            String[] names = signUpRequest.getFullName().trim().split(" ", 2);
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : "";

            // 2. Crear usuario en BD local
            User user = new User(
                    firstName,
                    lastName,
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword())
            );

            user.setPhone(signUpRequest.getPhone());

            // 3. Asignar rol (por defecto STUDENT)
            Set<String> strRoles = signUpRequest.getRole();
            Set<Role> roles = new HashSet<>();
            AtomicReference<String> mainRoleName = new AtomicReference<>(RoleName.ROLE_STUDENT.name());

            if (strRoles == null || strRoles.isEmpty()) {
                Role userRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                        .orElseThrow(() -> new RuntimeException("Error: Rol STUDENT no encontrado."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role.toLowerCase()) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Rol ADMIN no encontrado."));
                            roles.add(adminRole);
                            mainRoleName.set(RoleName.ROLE_ADMIN.name());
                            break;
                        case "instructor":
                            Role instructorRole = roleRepository.findByName(RoleName.ROLE_INSTRUCTOR)
                                    .orElseThrow(() -> new RuntimeException("Error: Rol INSTRUCTOR no encontrado."));
                            roles.add(instructorRole);
                            mainRoleName.set(RoleName.ROLE_INSTRUCTOR.name());
                            break;
                        default:
                            Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                                    .orElseThrow(() -> new RuntimeException("Error: Rol STUDENT no encontrado."));
                            roles.add(studentRole);
                    }
                });
            }

            user.setRoles(roles);
            user.setEnabled(true);
            userRepository.save(user);

            // 4. Sincronizar con Auth0
            try {
                auth0Service.createAuth0User(
                        signUpRequest.getEmail(),
                        signUpRequest.getPassword(),
                        signUpRequest.getFullName(),
                        mainRoleName.get()
                );
            } catch (Exception e) {
                // Log pero no fallar. El usuario podría ya existir en Auth0
                System.err.println("Advertencia: No se pudo crear usuario en Auth0: " + e.getMessage());
            }

            // 5. Enviar correo de bienvenida
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo enviar correo de bienvenida: " + e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Usuario registrado exitosamente! Por favor inicia sesión con Auth0."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al registrar usuario: " + e.getMessage()));
        }
    }

    /**
     * Solicitar recuperación de contraseña
     * POST /auth/forgot-password
     * Body: { "email": "user@example.com" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody LoginRequest request) {
        try {
            // Buscar usuario por email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Crear token temporal
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            tokenRepository.save(resetToken);

            // Enviar email
            emailService.sendPasswordResetEmail(user.getEmail(), token);

            return ResponseEntity.ok(new MessageResponse("Correo de recuperación enviado exitosamente!"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al procesar solicitud: " + e.getMessage()));
        }
    }

    /**
     * Restablecer contraseña con token
     * POST /auth/reset-password?token=xxxxx
     * Body: { "password": "newPassword" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestBody LoginRequest request) {
        try {
            // Validar token
            PasswordResetToken resetToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token inválido"));

            // Validar expiración
            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                tokenRepository.delete(resetToken);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El token ha expirado"));
            }

            // Actualizar contraseña
            User user = resetToken.getUser();
            user.setPassword(encoder.encode(request.getPassword()));
            userRepository.save(user);

            // Eliminar token usado
            tokenRepository.delete(resetToken);

            return ResponseEntity.ok(new MessageResponse("Contraseña restablecida exitosamente!"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Token inválido: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al restablecer contraseña: " + e.getMessage()));
        }
    }
}
