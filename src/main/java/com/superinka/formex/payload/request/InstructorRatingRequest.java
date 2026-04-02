package com.superinka.formex.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorRatingRequest {
    private Long instructorId;
    private Long courseId;
    private int rating;
    private String comment;
}


