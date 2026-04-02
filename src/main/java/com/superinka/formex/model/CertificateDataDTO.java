package com.superinka.formex.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CertificateDataDTO {

    private Long studentId;
    private String fullName;
    private Long courseId;
    private String courseName;
    private double attendancePercentage;
}
