package com.superinka.formex.service;

import com.superinka.formex.model.AttendanceRecord;
import com.superinka.formex.model.AttendanceSummaryDTO;
import com.superinka.formex.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository repo;

    public AttendanceService(AttendanceRepository repo) {
        this.repo = repo;
    }

    public List<AttendanceRecord> getRecordsForSession(Long sessionId) {
        return repo.findBySessionId(sessionId);
    }

    @Transactional
    public AttendanceRecord upsertRecord(
            Long sessionId,
            Long userId,
            AttendanceRecord.AttendanceStatus status,
            Long markedBy
    ) {
        Optional<AttendanceRecord> found =
                repo.findBySessionIdAndUserId(sessionId, userId);

        if (found.isPresent()) {
            AttendanceRecord r = found.get();
            r.setStatus(status);
            r.setMarkedBy(markedBy);
            r.setMarkedAt(OffsetDateTime.now());
            return repo.save(r);
        } else {
            AttendanceRecord r = new AttendanceRecord();
            r.setSessionId(sessionId);
            r.setUserId(userId);
            r.setStatus(status);
            r.setMarkedBy(markedBy);
            r.setMarkedAt(OffsetDateTime.now());
            return repo.save(r);
        }
    }

    @Transactional
    public int upsertBatch(Long sessionId, List<BatchItem> items, Long markedBy) {
        int count = 0;
        for (BatchItem it : items) {
            if (it.getStatus() == null) continue;

            upsertRecord(
                    sessionId,
                    it.getUserId(),
                    it.getStatus(),
                    markedBy
            );

            count++;
        }
        return count;
    }

    public static class BatchItem {
        private Long userId;
        private AttendanceRecord.AttendanceStatus status;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public AttendanceRecord.AttendanceStatus getStatus() {
            return status;
        }

        public void setStatus(AttendanceRecord.AttendanceStatus status) {
            this.status = status;
        }
    }

    public AttendanceSummaryDTO getAttendanceSummary(Long studentId, Long courseId) {
        List<Object[]> resultList = repo.getAttendanceSummary(studentId, courseId);

        if (resultList == null || resultList.isEmpty()) {
            return new AttendanceSummaryDTO(0, 0, 0, 0, 0);
        }

        Object[] result = resultList.get(0);

        long total = ((Number) result[0]).longValue();
        long p     = ((Number) result[1]).longValue();
        long a     = ((Number) result[2]).longValue();

        double percentage = total == 0 ? 0 : (p * 100.0) / total;

        long totalStudentSessions = p + a;
        return new AttendanceSummaryDTO(total, totalStudentSessions, p, a, percentage);
    }
    }