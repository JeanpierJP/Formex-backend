package com.superinka.formex.payload.response;

public class ResourceResponse {
    private Long id;
    private String title;
    private String description;
    private String link;

    public ResourceResponse(Long id, String title, String description, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLink() { return link; }
}
