package com.superinka.formex.service;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.superinka.formex.model.CertificateDataDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CertificateService {

    public byte[] generateCertificate(CertificateDataDTO data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Fecha actual
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Título en negrita y más grande (sin TextAlignment)
            document.add(new Paragraph("🎓 CERTIFICADO DE FINALIZACIÓN")
                    .setBold()
                    .setFontSize(20));

            document.add(new Paragraph("\nSe certifica que: " + data.getFullName())
                    .setFontSize(14));

            document.add(new Paragraph("Ha completado satisfactoriamente el curso: " + data.getCourseName())
                    .setFontSize(14));

            document.add(new Paragraph("Con un porcentaje de asistencia de: " + data.getAttendancePercentage() + "%")
                    .setFontSize(14));

            document.add(new Paragraph("Fecha de emisión: " + date)
                    .setFontSize(12));

            document.add(new Paragraph("\nEste certificado acredita que el alumno ha cumplido con los requisitos del curso.")
                    .setFontSize(12));

            // Espacio para firma (sin centrado)
            document.add(new Paragraph("\n\n__________________________"));
            document.add(new Paragraph("Firma del Instructor"));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando certificado", e);
        }
    }
}
