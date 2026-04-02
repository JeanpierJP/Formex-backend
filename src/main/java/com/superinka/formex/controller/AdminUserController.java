package com.superinka.formex.controller;

import com.superinka.formex.model.*;
import com.superinka.formex.model.enums.PaymentStatus;
import com.superinka.formex.model.enums.RoleName;
import com.superinka.formex.payload.request.CreateUserRequest;
import com.superinka.formex.payload.request.UpdateUserRequest;
import com.superinka.formex.payload.response.MessageResponse;
import com.superinka.formex.payload.response.StudentDto;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.RoleRepository;
import com.superinka.formex.repository.UserCourseRepository;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.Auth0Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final UserCourseRepository userCourseRepository;
    private final CourseRepository courseRepository;
    private final Auth0Service auth0Service;

    // Listar usuarios con filtro opcional por rol
    @GetMapping
    public List<User> getAllUsers(@RequestParam(required = false) String role) {
        if (role != null && !role.isEmpty()) {
            try {
                String roleNameStr = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                RoleName roleName = RoleName.valueOf(roleNameStr.toUpperCase());
                return userRepository.findByRoles_Name(roleName);
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return userRepository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: El email ya está en uso"));
        }

        // 1. Intentar crear en Auth0 PRIMERO
        // Si la contraseña es débil, Auth0 fallará aquí y no ensuciaremos nuestra BD local
        try {
            auth0Service.createAuth0User(
                    createUserRequest.getEmail(),
                    createUserRequest.getPassword(),
                    createUserRequest.getName() + " " + createUserRequest.getLastname(),
                    createUserRequest.getRole()
            );
        } catch (Exception e) {
            // Si Auth0 falla (ej. Password too weak), retornamos error y cancelamos todo
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error al crear en Auth0: " + e.getMessage()));
        }

        // 2. Si Auth0 aceptó, guardamos en BD Local
        User user = new User(
                createUserRequest.getName(),
                createUserRequest.getLastname(),
                createUserRequest.getEmail(),
                encoder.encode(createUserRequest.getPassword())
        );

        user.setPoints(0);
        user.setTeachingHours(0);

        Set<Role> roles = new HashSet<>();
        String strRole = createUserRequest.getRole();

        if (strRole == null) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            switch (strRole.toLowerCase()) {
                case "admin":
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                    break;
                case "instructor":
                    Role modRole = roleRepository.findByName(RoleName.ROLE_INSTRUCTOR)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(modRole);
                    break;
                default:
                    Role userRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
            }
        }

        user.setRoles(roles);
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario creado exitosamente"));
    }

    // ... (Resto de métodos update, delete, etc. se mantienen igual)
    // Solo estoy modificando createUser para mejorar el flujo de validación.

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        user.setName(updateUserRequest.getName());
        user.setLastname(updateUserRequest.getLastname());
        user.setPhone(updateUserRequest.getPhone());
        user.setEnabled(updateUserRequest.getEnabled());

        // Actualizar rol
        RoleName roleName = RoleName.valueOf("ROLE_" + updateUserRequest.getRole().toUpperCase());
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        user.setRoles(new HashSet<>(List.of(role))); // ✅ mutable Set

        // Guardar en BD
        User saved = userRepository.save(user);

        // 🔥 Sincronizar con Auth0
        if (saved.getAuth0Id() != null) {
            auth0Service.updateAuth0User(saved.getAuth0Id(), roleName.name(), saved.getEnabled());
        }

        return ResponseEntity.ok(new MessageResponse("Usuario actualizado exitosamente"));
    }


    @GetMapping("/students")
    public List<StudentDto> getAllStudents() {
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role Student not found."));

        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(studentRole) && Boolean.TRUE.equals(u.getEnabled()))
                .map(u -> new StudentDto(
                        u.getId(),
                        u.getName() + " " + u.getLastname(),
                        u.getEmail(),
                        u.getPhone(),
                        null, // o PaymentStatus si aplica
                        u.getPoints() != null ? u.getPoints() : 0
                ))
                .toList();
    }


    @PutMapping("/{userId}/assign-course/{courseId}")
    public ResponseEntity<?> assignCourseToStudent(@PathVariable Long userId, @PathVariable Long courseId) {
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado"));

        UserCourseId id = new UserCourseId(userId, courseId);

        if (userCourseRepository.existsById(id)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("El estudiante ya está matriculado en este curso"));
        }

        UserCourse uc = new UserCourse();
        uc.setId(id);
        uc.setUser(student);
        uc.setCourse(course);
        uc.setPaymentStatus(PaymentStatus.PAID);

        userCourseRepository.save(uc);

        return ResponseEntity.ok(new MessageResponse("Curso asignado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        user.setEnabled(false);
        userRepository.save(user);

        // 🔥 Desactivar también en Auth0
        if (user.getAuth0Id() != null) {
            auth0Service.disableAuth0User(user.getAuth0Id());
        }

        return ResponseEntity.ok(new MessageResponse("Usuario desactivado exitosamente."));
    }
}