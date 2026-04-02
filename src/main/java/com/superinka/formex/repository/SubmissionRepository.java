package com.superinka.formex.repository;

import com.superinka.formex.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByEvaluationIdAndStudentId(Long evaluationId, Long studentId);

    List<Submission> findByEvaluationId(Long evaluationId);
}


