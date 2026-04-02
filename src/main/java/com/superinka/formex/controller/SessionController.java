package com.superinka.formex.controller;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.Session;
import com.superinka.formex.model.User;
import com.superinka.formex.payload.request.SessionRequest;
import com.superinka.formex.payload.response.MessageResponse;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.SessionRepository;
import com.superinka.formex.service.InstructorGamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {

    private final SessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final InstructorGamificationService instructorGamificationService;

    //Listar sesiones de un curso(Publico para ver el tema horario)
    @GetMapping("/public/courses/{courseId}/sessions")
    public List<Session> getCourseSessions(@PathVariable Long courseId) {
        return sessionRepository.findByCourseIdAndEnabledTrueOrderByStartTimeAsc(courseId);
    }

    // --- AGREGAR ESTE MÉTODO PARA EDITAR ---
    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @RequestBody SessionRequest request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setStartTime(request.getStartTime());
        session.setDurationMinutes(request.getDurationMinutes());
        session.setMeetingLink(request.getMeetingLink());
        session.setRecordingLink(request.getRecordingLink());

        sessionRepository.save(session);

        return ResponseEntity.ok(new MessageResponse("Sesión actualizada exitosamente"));
    }

    //Crear sesion (Solo admin o instructor)
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<?> createSession(@RequestBody SessionRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        Session session = Session.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .durationMinutes(request.getDurationMinutes())
                .meetingLink(request.getMeetingLink())
                .recordingLink(request.getRecordingLink())
                .course(course)
                .isCompleted(false)
                .enabled(true)
                .build();
        sessionRepository.save(session);

// 🔥 GAMIFICACIÓN DEL INSTRUCTOR (SOLO SESIONES PASADAS)
        User instructor = course.getInstructor();
        if (instructor != null && session.getStartTime().isBefore(LocalDateTime.now())) {
            instructorGamificationService.addTeachingMinutes(
                    instructor,
                    session.getDurationMinutes()
            );
        }

        return ResponseEntity.ok(new MessageResponse("Sesion agendada exitosamente"));
    }

    //Eliminar Sesion
    @DeleteMapping("sessions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sesion no encontrada"));

        //Soft delete: solo se cambia el estado
        session.setEnabled(false);

        sessionRepository.save(session);

        return ResponseEntity.ok(new MessageResponse("Sesion eliminada del calendario"));
    }
}
