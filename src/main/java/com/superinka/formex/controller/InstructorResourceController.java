package com.superinka.formex.controller;

import com.superinka.formex.model.Resource;
import com.superinka.formex.payload.request.ResourceRequest;
import com.superinka.formex.payload.response.ResourceResponse;
import com.superinka.formex.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/courses/{courseId}/resources")
@RequiredArgsConstructor
public class InstructorResourceController {

    private final ResourceService resourceService;

    // 🔹 LISTAR
    @GetMapping
    public List<ResourceResponse> getResources(@PathVariable Long courseId) {
        return resourceService.getByCourse(courseId);
    }

    // 🔹 CREAR
    @PostMapping
    public Resource createResource(
            @PathVariable Long courseId,
            @RequestBody ResourceRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return resourceService.create(courseId, request, jwt);
    }


    // 🔹 ELIMINAR
    @DeleteMapping("/{resourceId}")
    public void deleteResource(
            @PathVariable Long courseId,
            @PathVariable Long resourceId
    ) {
        resourceService.delete(courseId, resourceId);
    }
}

