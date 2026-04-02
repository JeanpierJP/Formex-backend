package com.superinka.formex.service;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.Evaluation;
import com.superinka.formex.payload.request.EvaluationRequest;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final CourseRepository courseRepository;

    public Evaluation createEvaluation(
            Long courseId,
            EvaluationRequest request,
            MultipartFile file
    ) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // ⚠️ Aquí luego puedes usar S3, Cloudinary, etc.
        String fileUrl = savePdf(file);

        Evaluation evaluation = new Evaluation();
        evaluation.setTitle(request.getTitle());
        evaluation.setDescription(request.getDescription());
        evaluation.setFileUrl(fileUrl);
        evaluation.setCourse(course);

        return evaluationRepository.save(evaluation);
    }

    public List<Evaluation> getByCourse(Long courseId) {
        return evaluationRepository.findByCourseId(courseId);
    }

    private String savePdf(MultipartFile file) {
        try {
            String uploadDir = "uploads/evaluations";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.write(filePath, file.getBytes());

            // Devuelve ruta absoluta para evitar problemas
            return filePath.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Error guardando el PDF", e);
        }
    }


    public void delete(Long courseId, Long evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));

        // 🔒 Seguridad extra: verificar que pertenezca al curso
        if (!evaluation.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("La evaluación no pertenece a este curso");
        }

        evaluationRepository.delete(evaluation);
    }

    public Evaluation getByIdAndCourse(Long evaluationId, Long courseId) {
        return evaluationRepository
                .findByIdAndCourseId(evaluationId, courseId)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));
    }

    public Resource downloadFile(Long courseId, Long evaluationId) {

        Evaluation evaluation = evaluationRepository
                .findByIdAndCourseId(evaluationId, courseId)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));

        Path path = Paths.get(evaluation.getFileUrl());

        if (!Files.exists(path)) {
            throw new RuntimeException("Archivo no encontrado en el servidor");
        }

        return new FileSystemResource(path);
    }

}
