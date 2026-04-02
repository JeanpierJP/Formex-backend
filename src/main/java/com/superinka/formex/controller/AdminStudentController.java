package com.superinka.formex.controller;

import com.superinka.formex.model.enums.RoleName;
import com.superinka.formex.payload.response.StudentDto;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentController {

    private final UserRepository userRepository;
    private final StudentService studentService;

    @GetMapping
    public List<StudentDto> getAllStudents() {

        return userRepository.findAll().stream()
                .filter(user ->
                        user.getRoles().stream()
                                .anyMatch(r -> r.getName().name().equals("ROLE_STUDENT"))
                )
                .map(user -> new StudentDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        null,        // paymentStatus (no aplica aquí)
                        0.0,         // attendancePercentage
                        user.getPoints()
                ))
                .toList();
    }

    // 🔹 NUEVO ENDPOINT: desinscribir alumno de un curso
    @DeleteMapping("/courses/{courseId}/students/{studentId}")
    public ResponseEntity<String> unenrollStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId
    ) {
        studentService.removeCourseFromStudent(studentId, courseId);
        return ResponseEntity.ok("Alumno desinscrito y puntos actualizados si correspondía");
    }
}

