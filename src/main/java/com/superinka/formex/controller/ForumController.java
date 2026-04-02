package com.superinka.formex.controller;

import com.superinka.formex.model.ForumMessage;
import com.superinka.formex.service.ForumService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/resources/{resourceId}/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    // 🔹 Obtener mensajes
    @GetMapping
    public List<ForumMessage> getMessages(@PathVariable Long courseId,
                                          @PathVariable Long resourceId) {
        return forumService.getMessages(courseId, resourceId);
    }

    // 🔹 Crear mensaje
    @PostMapping
    public ForumMessage createMessage(@PathVariable Long courseId,
                                      @PathVariable Long resourceId,
                                      @RequestBody ForumMessage message) {
        return forumService.createMessage(courseId, resourceId, message.getAuthor(), message.getContent());
    }

    // 🔹 Eliminar mensaje (solo instructor/admin)
    @DeleteMapping("/{messageId}")
    public void deleteMessage(@PathVariable Long messageId) {
        forumService.deleteMessage(messageId);
    }
}
