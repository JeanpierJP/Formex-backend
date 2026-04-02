package com.superinka.formex.controller;

import com.superinka.formex.payload.request.EvaluationRequest;
import com.superinka.formex.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/courses/{courseId}/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    // 👨‍🏫 SOLO INSTRUCTOR
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @PathVariable Long courseId,
            @RequestPart("data") EvaluationRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        evaluationService.createEvaluation(courseId, request, file);
        return ResponseEntity.ok().build();
    }

    // 👨‍🎓 INSTRUCTOR y ALUMNO
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                evaluationService.getByCourse(courseId)
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{evaluationId}")
    public ResponseEntity<?> delete(
            @PathVariable Long courseId,
            @PathVariable Long evaluationId
    ) {
        evaluationService.delete(courseId, evaluationId);
        return ResponseEntity.noContent().build();
    }

    // 👨‍🎓 INSTRUCTOR y ALUMNO - ver una evaluación
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @GetMapping("/{evaluationId}")
    public ResponseEntity<?> getOne(
            @PathVariable Long courseId,
            @PathVariable Long evaluationId
    ) {
        return ResponseEntity.ok(
                evaluationService.getByIdAndCourse(evaluationId, courseId)
        );
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @GetMapping("/{evaluationId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long courseId,
            @PathVariable Long evaluationId
    ) {

        Resource file = evaluationService.downloadFile(courseId, evaluationId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

}


