package com.superinka.formex.payload.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionRequest {

    private String title;
    private LocalDateTime startTime;
    private Integer durationMinutes;
    private String meetingLink;
    private Long courseId;
    private String description;
    private String recordingLink;

}
