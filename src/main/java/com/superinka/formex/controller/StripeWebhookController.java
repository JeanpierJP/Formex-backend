package com.superinka.formex.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.superinka.formex.model.Course;
import com.superinka.formex.model.User;
import com.superinka.formex.model.UserCourse;
import com.superinka.formex.model.UserCourseId;
import com.superinka.formex.model.enums.PaymentStatus;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.UserCourseRepository;
import com.superinka.formex.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    // ======================================================
    // WEBHOOK STRIPE
    // ======================================================
    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {

        System.out.println("🚀 Stripe Webhook recibido");

        try {
            // 🔐 Verificación de firma
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            System.out.println("📦 Evento: " + event.getType());

            // 🔹 Solo nos interesa este evento
            if (!"checkout.session.completed".equals(event.getType())) {
                return ResponseEntity.ok("Evento ignorado");
            }

            // 🔹 Obtener sesión
            Session session = (Session) event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Sesión inválida"));

            // 🔹 Validar pago REAL
            if (!"paid".equals(session.getPaymentStatus())) {
                System.out.println("⚠️ Sesión no pagada");
                return ResponseEntity.ok("Pago no confirmado");
            }

            // 🔹 Metadata obligatoria
            Map<String, String> metadata = session.getMetadata();
            if (metadata == null ||
                    !metadata.containsKey("userId") ||
                    !metadata.containsKey("courseId")) {

                System.out.println("❌ Metadata faltante");
                return ResponseEntity.ok("Metadata inválida");
            }

            Long userId = Long.valueOf(metadata.get("userId"));
            Long courseId = Long.valueOf(metadata.get("courseId"));

            // 🔹 Buscar entidades reales
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            // 🔹 Clave compuesta
            UserCourseId id = new UserCourseId(userId, courseId);

            // 🔹 Buscar o crear inscripción
            UserCourse userCourse = userCourseRepository.findById(id)
                    .orElseGet(() -> {
                        UserCourse uc = new UserCourse();
                        uc.setId(id);
                        uc.setUser(user);
                        uc.setCourse(course);
                        uc.setPaymentStatus(PaymentStatus.PENDING);
                        return uc;
                    });

            // 🔹 Idempotencia real
            if (userCourse.getPaymentStatus() == PaymentStatus.PAID) {
                System.out.println("⚠️ Pago ya procesado anteriormente");
                return ResponseEntity.ok("Pago duplicado ignorado");
            }

            // 🔹 Confirmar pago
            userCourse.setPaymentStatus(PaymentStatus.PAID);
            userCourseRepository.save(userCourse);

            System.out.println("✅ PAGO CONFIRMADO Y BD ACTUALIZADA");
            return ResponseEntity.ok("OK");

        } catch (SignatureVerificationException e) {
            System.out.println("❌ Firma Stripe inválida");
            return ResponseEntity.badRequest().body("Firma inválida");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error procesando webhook");
        }
    }
}
