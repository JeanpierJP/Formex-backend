package com.superinka.formex.controller;

import com.superinka.formex.model.User;
import com.superinka.formex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorController {

    private final UserRepository userRepository;

    private User getUserFromJwt(Jwt jwt) {
        String auth0Id = jwt.getClaimAsString("sub");
        return userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));
    }

    // 👨‍🏫 PERFIL DEL INSTRUCTOR (PUNTOS)
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal Jwt jwt) {

        User instructor = getUserFromJwt(jwt);

        Map<String, Object> response = Map.of(
                "id", instructor.getId(),
                "fullName", instructor.getFullName(),
                "email", instructor.getEmail(),
                "points", instructor.getPoints() != null ? instructor.getPoints() : 0
        );

        return ResponseEntity.ok(response);
    }
}
