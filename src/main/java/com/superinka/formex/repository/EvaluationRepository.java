package com.superinka.formex.repository;

import com.superinka.formex.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByCourseId(Long courseId);
    Optional<Evaluation> findByIdAndCourseId(Long evaluationId, Long courseId);

}

