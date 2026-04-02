package com.superinka.formex.controller;

import com.superinka.formex.model.AttendanceRecord;
import com.superinka.formex.model.Session;
import com.superinka.formex.payload.response.StudentDto;
import com.superinka.formex.service.AttendanceService;
import com.superinka.formex.service.SessionStudentService;
import com.superinka.formex.repository.SessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Endpoints:
 * GET  /api/sessions/{sessionId}/attendance        -> lista de alumnos + estado actual (PRESENT/ABSENT/null)
 * POST /api/sessions/{sessionId}/attendance        -> batch upsert
 * POST /api/sessions/{sessionId}/attendance/{studentId} -> upsert single
 *
 * NOTA: requiere ajustar la obtención del userId desde la autenticación real.
 */
@RestController
@RequestMapping("/api/sessions/{sessionId}/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SessionStudentService studentService;
    private final SessionRepository sessionRepository;

    public AttendanceController(
            AttendanceService attendanceService,
            SessionStudentService studentService,
            SessionRepository sessionRepository
    ) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getAttendance(@PathVariable Long sessionId) {

        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        List<StudentDto> students =
                studentService.getStudentsForSession(sessionId);

        List<AttendanceRecord> records =
                attendanceService.getRecordsForSession(sessionId);

        Map<Long, AttendanceRecord.AttendanceStatus> statusMap =
                records.stream().collect(Collectors.toMap(
                        AttendanceRecord::getUserId,
                        AttendanceRecord::getStatus
                ));

        List<Map<String, Object>> response = students.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", s.getId());
            m.put("fullName", s.getFullName());
            m.put("email", s.getEmail());
            m.put("phone", s.getPhone());
            m.put("status", statusMap.get(s.getId()));
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "students", response
        ));
    }

    // ===== BATCH =====
    public static class BatchRequest {
        private List<SingleItem> attendance;
        public List<SingleItem> getAttendance() { return attendance; }
        public void setAttendance(List<SingleItem> attendance) { this.attendance = attendance; }
    }

    public static class SingleItem {
        private Long userId;
        private AttendanceRecord.AttendanceStatus status;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public AttendanceRecord.AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceRecord.AttendanceStatus status) { this.status = status; }
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> postBatch(
            @PathVariable Long sessionId,
            @RequestBody BatchRequest body
    ) {
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        Long markedBy = getCurrentUserIdOrNull();

        List<AttendanceService.BatchItem> items = new ArrayList<>();
        if (body.getAttendance() != null) {
            for (SingleItem it : body.getAttendance()) {
                AttendanceService.BatchItem bi =
                        new AttendanceService.BatchItem();
                bi.setUserId(it.getUserId());
                bi.setStatus(it.getStatus());
                items.add(bi);
            }
        }

        int updated =
                attendanceService.upsertBatch(sessionId, items, markedBy);

        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> postSingle(
            @PathVariable Long sessionId,
            @PathVariable Long userId,
            @RequestBody SingleItem body
    ) {
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        if (body.getStatus() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "status es requerido"));
        }

        AttendanceRecord record =
                attendanceService.upsertRecord(
                        sessionId,
                        userId,
                        body.getStatus(),
                        getCurrentUserIdOrNull()
                );

        return ResponseEntity.ok(Map.of("record", record));
    }

    private Long getCurrentUserIdOrNull() {
        return null; // luego JWT
    }
}
