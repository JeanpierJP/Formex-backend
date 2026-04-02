package com.superinka.formex.controller;

import com.superinka.formex.model.UserCourse;
import com.superinka.formex.model.UserCourseId;
import com.superinka.formex.model.enums.PaymentStatus;
import com.superinka.formex.repository.UserCourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CoursePaymentController {

    private final UserCourseRepository userCourseRepository;

    @PutMapping("/{courseId}/payments/status")
    @Transactional
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> payload) {

        Long studentId = Long.valueOf(payload.get("studentId").toString());
        String status = payload.get("status").toString();

        UserCourseId id = new UserCourseId(studentId, courseId);

        UserCourse userCourse = userCourseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        userCourse.setPaymentStatus(PaymentStatus.valueOf(status));
        userCourseRepository.save(userCourse);

        return ResponseEntity.ok().build();
    }
}

