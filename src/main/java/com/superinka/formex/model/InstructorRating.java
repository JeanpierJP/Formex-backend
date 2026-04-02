package com.superinka.formex.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"instructor_id", "student_id", "month", "year"}
        )
)
public class InstructorRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User instructor;

    @ManyToOne
    private User student;

    @ManyToOne
    private Course course;

    private int rating;
    private String comment;

    private int month;
    private int year;

    private LocalDateTime createdAt = LocalDateTime.now();
}

