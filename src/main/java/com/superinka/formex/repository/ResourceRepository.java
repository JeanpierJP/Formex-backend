package com.superinka.formex.repository;

import com.superinka.formex.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByCourseId(Long courseId);
}
