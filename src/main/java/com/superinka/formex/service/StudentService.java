package com.superinka.formex.service;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.User;
import com.superinka.formex.model.UserCourse;
import com.superinka.formex.model.UserCourseId;
import com.superinka.formex.model.enums.PaymentStatus;
import com.superinka.formex.repository.UserCourseRepository;
import com.superinka.formex.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;

    public List<Course> getPaidCourses(Long studentId) {

        return userCourseRepository
                .findByUserIdAndPaymentStatus(studentId, PaymentStatus.PAID)
                .stream()
                .map(UserCourse::getCourse)
                .toList();
    }

    @Transactional
    public void removeCourseFromStudent(Long studentId, Long courseId) {
        UserCourseId id = new UserCourseId(studentId, courseId);
        UserCourse userCourse = userCourseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado para el usuario"));

        if (userCourse.getPaymentStatus() == PaymentStatus.PAID) {
            var user = userCourse.getUser();
            int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(Math.max(currentPoints - 100, 0));
            userRepository.save(user);
        }

        userCourseRepository.delete(userCourse);
    }

    @Transactional
    public void awardCertificate(Long studentId, Long courseId) {

        UserCourseId id = new UserCourseId(studentId, courseId);

        UserCourse userCourse = userCourseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado para el usuario"));

        // ⛔ Evitar duplicar puntos
        if (Boolean.TRUE.equals(userCourse.getCertificateIssued())) {
            return;
        }

        // ==========================
        // 🎓 PUNTOS AL ALUMNO (+100)
        // ==========================
        User user = userCourse.getUser();
        int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        user.setPoints(currentPoints + 100);

        // ==========================
        // 👨‍🏫 PUNTOS AL INSTRUCTOR (+10)
        // ==========================
        Course course = userCourse.getCourse();
        User instructor = course.getInstructor();

        if (instructor != null && !instructor.getId().equals(user.getId())) {
            int instructorPoints = instructor.getPoints() != null ? instructor.getPoints() : 0;
            instructor.setPoints(instructorPoints + 10);
            userRepository.save(instructor);
        }

        // ==========================
        // ✅ MARCAR CERTIFICADO
        // ==========================
        userCourse.setCertificateIssued(true);

        userRepository.save(user);
        userCourseRepository.save(userCourse);
    }
}

