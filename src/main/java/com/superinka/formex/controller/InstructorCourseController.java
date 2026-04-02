package com.superinka.formex.controller;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.User;
import com.superinka.formex.payload.response.StudentDto;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/courses")
@RequiredArgsConstructor
public class InstructorCourseController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public List<StudentDto> getStudents(@PathVariable Long id) {
        return userRepository.findStudentsByCourseId(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public List<Course> getInstructorCourses(@AuthenticationPrincipal Jwt jwt) {
        // Obtener el instructor a partir del JWT
        String auth0Id = jwt.getSubject();
        User instructor = userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

        // Retornar cursos habilitados de este instructor
        return courseRepository.findByInstructorAndEnabledTrue(instructor);
    }


}
