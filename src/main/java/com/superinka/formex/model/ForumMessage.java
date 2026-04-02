package com.superinka.formex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_messages")
public class ForumMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;
    private Long resourceId;
    private String author;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 🔹 Constructores
    public ForumMessage() {}

    public ForumMessage(Long courseId, Long resourceId, String author, String content) {
        this.courseId = courseId;
        this.resourceId = resourceId;
        this.author = author;
        this.content = content;
    }

    // 🔹 Getters y Setters
    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
