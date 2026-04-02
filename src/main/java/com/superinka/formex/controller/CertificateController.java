package com.superinka.formex.controller;

import com.superinka.formex.model.CertificateDataDTO;
import com.superinka.formex.service.CertificateService;
import com.superinka.formex.service.StudentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;
    private final StudentService studentService;

    public CertificateController(
            CertificateService certificateService,
            StudentService studentService
    ) {
        this.certificateService = certificateService;
        this.studentService = studentService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateCertificate(@RequestBody CertificateDataDTO data) {
        if (data.getAttendancePercentage() < 85) {
            return ResponseEntity.badRequest()
                    .body(("El alumno no cumple el 85% de asistencia").getBytes());
        }

        // 🎮 Regla de puntos (solo si llegó al 100%)
        if (data.getAttendancePercentage() >= 100) {
            studentService.awardCertificate(
                    data.getStudentId(),
                    data.getCourseId()
            );
        }

        byte[] pdfBytes = certificateService.generateCertificate(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificado_" + data.getFullName() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
