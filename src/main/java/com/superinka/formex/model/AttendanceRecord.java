package com.superinka.formex.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_session_user",
                        columnNames = {"session_id", "user_id"}
                )
        }
)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 sesión
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    // 🔗 usuario (ANTES student_id)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 📌 estado de asistencia
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    // 👤 quién marcó
    @Column(name = "marked_by")
    private Long markedBy;

    // ⏰ cuándo se marcó
    @Column(name = "marked_at")
    private OffsetDateTime markedAt;

    // ✅ ENUM CORRECTO
    public enum AttendanceStatus {
        PRESENT,
        ABSENT
    }

    // 🔁 Fecha automática
    @PrePersist
    public void prePersist() {
        if (markedAt == null) {
            markedAt = OffsetDateTime.now();
        }
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Long getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(Long markedBy) {
        this.markedBy = markedBy;
    }

    public OffsetDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(OffsetDateTime markedAt) {
        this.markedAt = markedAt;
    }
}
