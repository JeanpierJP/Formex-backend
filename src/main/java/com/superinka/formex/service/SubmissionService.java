package com.superinka.formex.service;

import com.superinka.formex.model.Evaluation;
import com.superinka.formex.model.Submission;
import com.superinka.formex.model.User;
import com.superinka.formex.payload.response.SubmissionInstructorDTO;
import com.superinka.formex.repository.EvaluationRepository;
import com.superinka.formex.repository.SubmissionRepository;
import com.superinka.formex.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;

    // Carpeta pública para servir archivos
    private final String uploadDir = "src/main/resources/static/uploads/submissions";

    public SubmissionService(SubmissionRepository submissionRepository,
                             UserRepository userRepository,
                             EvaluationRepository evaluationRepository) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.evaluationRepository = evaluationRepository;
    }

    // 🧑‍🎓 Subir entrega
    public Submission createSubmission(Long evaluationId,
                                       String comment,
                                       MultipartFile file,
                                       Long studentId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Debe subir un archivo");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new RuntimeException("Solo se permiten archivos PDF");
        }

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluación no existe"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Alumno no existe"));


        // 📁 Crear carpeta si no existe
        Files.createDirectories(Paths.get(uploadDir));

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, filename);

        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        Submission submission = new Submission();
        submission.setComment(comment);

        // URL pública para el navegador
        submission.setFileUrl("/uploads/submissions/" + filename);

        submission.setStudent(student);
        submission.setEvaluation(evaluation);

        return submissionRepository.save(submission);
    }

    public Submission getMySubmission(Long evaluationId, Long studentId) {
        return submissionRepository
                .findByEvaluationIdAndStudentId(evaluationId, studentId)
                .orElse(null);
    }

    public List<Submission> getAllByEvaluation(Long evaluationId) {
        return submissionRepository.findByEvaluationId(evaluationId);
    }

    public void deleteSubmission(Long submissionId) {
        submissionRepository.deleteById(submissionId);
    }

    public Submission getById(Long submissionId) {
        return submissionRepository.findById(submissionId).orElse(null);
    }

    // <-- Este es el método que necesitas
    public Path getFilePath(Submission submission) {
        return Paths.get(uploadDir).resolve(Paths.get(submission.getFileUrl()).getFileName());
    }

    public List<SubmissionInstructorDTO> getAllByEvaluationForInstructor(Long evaluationId) {
        return submissionRepository.findByEvaluationId(evaluationId)
                .stream()
                .map(sub -> new SubmissionInstructorDTO(
                        sub.getId(),
                        sub.getStudent().getFullName(),
                        sub.getFileUrl(),
                        sub.getComment(),
                        sub.getGrade()
                ))
                .toList();
    }

    public void gradeSubmission(Long submissionId, Double grade, User instructor) {

        if (grade < 0 || grade > 20) {
            throw new RuntimeException("La nota debe estar entre 0 y 20");
        }

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));

        submission.setGrade(grade);

        // ⭐ REGLA: si nota >= 17 y aún no fue premiada
        if (grade >= 17 && !submission.isRewarded()) {
            instructor.setPoints(instructor.getPoints() + 5);
            submission.setRewarded(true);
            userRepository.save(instructor);
        }

        submissionRepository.save(submission);
    }



}

