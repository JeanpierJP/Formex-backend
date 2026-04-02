package com.superinka.formex.controller;

import com.superinka.formex.model.Submission;
import com.superinka.formex.model.User;
import com.superinka.formex.service.SubmissionService;
import com.superinka.formex.repository.UserRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/courses/{courseId}/evaluations/{evaluationId}")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserRepository userRepository;

    public SubmissionController(SubmissionService submissionService,
                                UserRepository userRepository) {
        this.submissionService = submissionService;
        this.userRepository = userRepository;
    }

    // 🧑‍🎓 Subir entrega
    @PostMapping("/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submit(
            @PathVariable Long evaluationId,
            @RequestParam("comment") String comment,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        // 🔹 Obtener el auth0Id desde el JWT
        String auth0Id = jwt.getClaimAsString("sub");

        // 🔹 Buscar usuario por auth0Id
        User student = userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Alumno no existe"));

        // 🔹 Usar su ID interno para crear la entrega
        return ResponseEntity.ok(
                submissionService.createSubmission(
                        evaluationId,
                        comment,
                        file,
                        student.getId()
                )
        );
    }

    // 🧑‍🎓 Ver solo SU entrega
    @GetMapping("/my-submission")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> mySubmission(
            @PathVariable Long evaluationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String auth0Id = jwt.getClaimAsString("sub");

        User student = userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Alumno no existe"));

        return ResponseEntity.ok(
                submissionService.getMySubmission(
                        evaluationId,
                        student.getId()
                )
        );
    }

    // 👨‍🏫 Ver TODAS las entregas
    @GetMapping("/submissions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> all(@PathVariable Long evaluationId) {
        return ResponseEntity.ok(
                submissionService.getAllByEvaluationForInstructor(evaluationId)
        );
    }


    // 👨‍🏫 Eliminar entrega
    @DeleteMapping("/submissions/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        submissionService.deleteSubmission(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/submissions/download/{submissionId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','STUDENT')")
    public ResponseEntity<Resource> downloadSubmission(@PathVariable Long submissionId) throws IOException {
        Submission submission = submissionService.getById(submissionId);
        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        Path path = submissionService.getFilePath(submission);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());

        // Obtener extensión real del archivo original
        String originalFilename = path.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF) // si solo aceptas PDF
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + originalFilename + "\"")
                .body(resource);
    }

    @PutMapping("/submissions/{id}/grade")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long id,
            @RequestParam Double grade,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String auth0Id = jwt.getClaimAsString("sub");

        User instructor = userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

        submissionService.gradeSubmission(id, grade, instructor);
        return ResponseEntity.ok().build();
    }

}
