package com.superinka.formex.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.superinka.formex.model.Course;
import com.superinka.formex.model.User;
import com.superinka.formex.model.UserCourse;
import com.superinka.formex.model.UserCourseId;
import com.superinka.formex.model.enums.PaymentStatus;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.UserCourseRepository;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/stripe")
@RequiredArgsConstructor
public class StripePaymentController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;

    @PostMapping("/create-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Long> payload) {
        try {
            Stripe.apiKey = stripeSecretKey;

            Long courseId = payload.get("courseId");
            if (courseId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "courseId es obligatorio"));
            }

            // 🔑 Obtener el usuario autenticado desde el JWT de Auth0
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();

            String auth0UserId;
            Long userId;

            if (principal instanceof Jwt jwt) {
                // 🔑 Identificador único de Auth0
                String auth0Id = jwt.getSubject();
                String email = jwt.getClaimAsString("email");

                // Buscar primero por auth0Id
                User user = userRepository.findByAuth0Id(auth0Id)
                        .orElseGet(() -> {
                            if (email != null && !email.isBlank()) {
                                return userRepository.findByEmail(email.toLowerCase())
                                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado en BD"));
                            }
                            throw new RuntimeException("Usuario no encontrado en BD");
                        });

                userId = user.getId();

            } else if (principal instanceof UserDetailsImpl userDetails) {
                userId = userDetails.getId();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            UserCourseId id = new UserCourseId(userId, courseId);

            UserCourse userCourse = userCourseRepository
                    .findById(id)
                    .orElseGet(() -> {
                        UserCourse uc = new UserCourse();
                        uc.setId(id);
                        uc.setUser(user);
                        uc.setCourse(course);
                        uc.setPaymentStatus(PaymentStatus.PENDING);
                        return userCourseRepository.save(uc);
                    });

            System.out.println("🧠 USER REAL EN BD");
            System.out.println("👉 userId BD: " + user.getId());
            System.out.println("👉 auth0Id: " + user.getAuth0Id());
            System.out.println("👉 email: " + user.getEmail());

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .putMetadata("userId", userId.toString())
                    .putMetadata("courseId", courseId.toString())
                    .setSuccessUrl("https://www.formex.digital/student?payment=success")
                    .setCancelUrl("https://www.formex.digital/course/" + courseId)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(course.getPrice().longValue() * 100)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(course.getTitle())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            return ResponseEntity.ok(Map.of("url", session.getUrl()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error creando sesión de Stripe: " + e.getMessage()));
        }
    }

}