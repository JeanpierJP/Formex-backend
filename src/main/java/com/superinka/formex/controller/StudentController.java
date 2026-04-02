package com.superinka.formex.controller;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.User;
import com.superinka.formex.model.UserCourse;
import com.superinka.formex.repository.UserCourseRepository;
import com.superinka.formex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;

    // 📚 Dashboard ESTUDIANTE → ver sus cursos
    @GetMapping("/courses")
    public ResponseEntity<?> getMyCourses(@AuthenticationPrincipal Jwt jwt) {
        String auth0Id = jwt.getClaimAsString("sub");

        User student = userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        List<Course> courses = userCourseRepository.findByUser_Id(student.getId())
                .stream()
                .map(UserCourse::getCourse)
                .filter(Course::getEnabled)
                .toList();

        return ResponseEntity.ok(courses);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal Jwt jwt) {
        String auth0Id = jwt.getClaimAsString("sub");
        User student = userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> response = Map.of(
                "id", student.getId(),
                "fullName", student.getFullName(), // <-- aquí cambio
                "email", student.getEmail(),
                "points", student.getPoints() != null ? student.getPoints() : 0
        );

        return ResponseEntity.ok(response);
    }
}
