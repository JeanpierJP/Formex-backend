package com.superinka.formex.payload.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private String level;
    private String imageUrl;
    private Long categoryId;
}
