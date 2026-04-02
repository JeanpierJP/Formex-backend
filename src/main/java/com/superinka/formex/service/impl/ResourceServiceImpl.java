package com.superinka.formex.service.impl;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.Resource;
import com.superinka.formex.model.User;
import com.superinka.formex.payload.request.ResourceRequest;
import com.superinka.formex.payload.response.ResourceResponse;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.ResourceRepository;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public List<ResourceResponse> getByCourse(Long courseId) {
        return resourceRepository.findByCourseId(courseId)
                .stream()
                .map(r -> new ResourceResponse(r.getId(), r.getTitle(), r.getDescription(), r.getLink()))
                .toList();
    }

    @Override
    public Resource create(Long courseId, ResourceRequest request, Jwt jwt) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        Resource resource = new Resource();
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setLink(request.getLink());
        resource.setCourse(course);

        Resource saved = resourceRepository.save(resource);

        // 🔥 GAMIFICACIÓN: +10 puntos por crear recurso
        User instructor = course.getInstructor();
        int currentPoints = instructor.getPoints() != null ? instructor.getPoints() : 0;
        instructor.setPoints(currentPoints + 10);
        userRepository.save(instructor);

        return saved;
    }

    @Override
    public void delete(Long courseId, Long resourceId) {

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));

        // 🔒 seguridad básica
        if (!resource.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Recurso no pertenece a este curso");
        }

        resourceRepository.delete(resource);
    }
}

