package com.superinka.formex.service.impl;

import com.superinka.formex.model.UserCourse;
import com.superinka.formex.payload.response.StudentDto;
import com.superinka.formex.repository.UserCourseRepository;
import com.superinka.formex.repository.SessionRepository;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.AttendanceService;
import com.superinka.formex.service.SessionStudentService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionStudentServiceImpl implements SessionStudentService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UserCourseRepository userCourseRepository;
    private final AttendanceService attendanceService;

    /**
     * Obtener lista de alumnos de un curso con su estado de pago y asistencia
     */
    @Override
    public List<StudentDto> getStudentsForCourse(Long courseId) {
        List<UserCourse> userCourses = userCourseRepository.findByCourse_Id(courseId);

        return userCourses.stream().map(uc -> {
            // Calcular porcentaje de asistencia (Manejo seguro de nulos)
            double percentage = 0.0;
            try {
                percentage = attendanceService
                        .getAttendanceSummary(uc.getUser().getId(), courseId)
                        .getAttendancePercentage();
            } catch (Exception e) {
                // Si falla el servicio de asistencia (ej. no hay registros), asumimos 0%
                percentage = 0.0;
            }

            // CORRECCIÓN: Usamos el constructor actualizado de StudentDto
            // (id, name, lastname, email, phone, status, percentage)
            return new StudentDto(
                    uc.getUser().getId(),
                    uc.getUser().getFullName(),
                    uc.getUser().getEmail(),
                    uc.getUser().getPhone(),
                    uc.getPaymentStatus(),
                    percentage,
                    uc.getUser().getPoints()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Obtener lista de alumnos de una sesión específica
     */
    @Override
    public List<StudentDto> getStudentsForSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .map(session -> getStudentsForCourse(session.getCourse().getId()))
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));
    }
}
