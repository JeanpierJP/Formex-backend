package com.superinka.formex.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmissionInstructorDTO {
    private Long submissionId;
    private String studentName;
    private String fileUrl;
    private String comment;
    private Double grade;
}
