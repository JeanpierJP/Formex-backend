package com.superinka.formex.repository;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategoryId(Long categoryId);

    List<Course> findByEnabledTrue();

    List<Course> findByInstructorAndEnabledTrue(User instructor);


    List<Course> findByInstructorIdAndEnabledTrue(Long instructorId);

    List<Course> findByInstructor_Id(Long instructorId);

    Long countByEnabledTrue();
}

