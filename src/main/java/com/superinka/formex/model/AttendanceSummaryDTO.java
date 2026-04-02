package com.superinka.formex.model;

public class AttendanceSummaryDTO {

    private long totalCourseSessions;
    private long totalStudentSessions;
    private long presentes;
    private long ausentes;
    private double attendancePercentage;

    public AttendanceSummaryDTO(long totalCourseSessions, long totalStudentSessions,
                                long presentes, long ausentes, double attendancePercentage) {
        this.totalCourseSessions = totalCourseSessions;
        this.totalStudentSessions = totalStudentSessions;
        this.presentes = presentes;
        this.ausentes = ausentes;
        this.attendancePercentage = attendancePercentage;
    }

    public long getTotalCourseSessions() {
        return totalCourseSessions;
    }

    public long getTotalStudentSessions() {
        return totalStudentSessions;
    }

    public long getPresentes() {
        return presentes;
    }

    public long getAusentes() {
        return ausentes;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
}
