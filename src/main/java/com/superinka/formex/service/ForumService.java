package com.superinka.formex.service;

import com.superinka.formex.model.ForumMessage;
import com.superinka.formex.repository.ForumMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumService {

    private final ForumMessageRepository repository;

    public ForumService(ForumMessageRepository repository) {
        this.repository = repository;
    }

    public List<ForumMessage> getMessages(Long courseId, Long resourceId) {
        return repository.findByCourseIdAndResourceIdOrderByCreatedAtAsc(courseId, resourceId);
    }

    public ForumMessage createMessage(Long courseId, Long resourceId, String author, String content) {
        ForumMessage message = new ForumMessage(courseId, resourceId, author, content);
        return repository.save(message);
    }

    public void deleteMessage(Long id) {
        repository.deleteById(id);
    }
}
