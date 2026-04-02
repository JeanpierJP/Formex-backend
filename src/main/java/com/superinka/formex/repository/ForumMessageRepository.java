package com.superinka.formex.repository;

import com.superinka.formex.model.ForumMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumMessageRepository extends JpaRepository<ForumMessage, Long> {
    List<ForumMessage> findByCourseIdAndResourceIdOrderByCreatedAtAsc(Long courseId, Long resourceId);
}
