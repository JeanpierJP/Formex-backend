package com.superinka.formex.repository;

import com.superinka.formex.model.InstructorRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRatingRepository
        extends JpaRepository<InstructorRating, Long> {

    boolean existsByInstructorIdAndStudentIdAndMonthAndYear(
            Long instructorId,
            Long studentId,
            int month,
            int year
    );
}
