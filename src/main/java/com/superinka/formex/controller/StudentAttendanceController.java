package com.superinka.formex.controller;

import com.superinka.formex.model.AttendanceSummaryDTO;
import com.superinka.formex.model.User;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository; // 👈 necesario para buscar el estudiante

    @GetMapping("/{courseId}/attendance-summary")
    public ResponseEntity<?> getSummary(@PathVariable Long courseId, @AuthenticationPrincipal Jwt jwt) {
        try {
            // Obtener el id de Auth0 desde el token
            String auth0Id = jwt.getSubject();

            // Buscar al estudiante en la BD
            User student = userRepository.findByAuth0Id(auth0Id)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            // Calcular el resumen de asistencia
            AttendanceSummaryDTO summary = attendanceService.getAttendanceSummary(student.getId(), courseId);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen de asistencia");
        }
    }
}
