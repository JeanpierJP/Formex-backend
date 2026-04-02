package com.superinka.formex.repository;

import com.superinka.formex.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByCourseIdAndEnabledTrueOrderByStartTimeAsc(Long courseId);
}
