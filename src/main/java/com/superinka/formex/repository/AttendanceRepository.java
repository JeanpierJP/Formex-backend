package com.superinka.formex.repository;

import com.superinka.formex.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findBySessionId(Long sessionId);

    Optional<AttendanceRecord> findBySessionIdAndUserId(Long sessionId, Long userId);

    @Query(
            value = """
            SELECT
               COUNT(cs.id) AS total_sesiones,
               SUM(CASE WHEN ar.status = 'PRESENT' THEN 1 ELSE 0 END) AS asistencias,
               SUM(CASE WHEN ar.status = 'ABSENT' OR ar.id IS NULL THEN 1 ELSE 0 END) AS inasistencias,
               ROUND(
                   (SUM(CASE WHEN ar.status = 'PRESENT' THEN 1 ELSE 0 END) / COUNT(cs.id)) * 100, 1
               ) AS porcentaje_asistencia
            FROM course_sessions cs
            LEFT JOIN attendance_records ar
                   ON ar.session_id = cs.id
                   AND ar.user_id = :userId
            WHERE cs.course_id = :courseId
              AND cs.enabled = true;
        """,
            nativeQuery = true
    )
    List<Object[]> getAttendanceSummary(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );
}