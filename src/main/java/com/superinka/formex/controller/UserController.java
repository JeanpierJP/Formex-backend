package com.superinka.formex.controller;

import com.superinka.formex.model.Role;
import com.superinka.formex.model.User;
import com.superinka.formex.model.enums.RoleName;
import com.superinka.formex.repository.RoleRepository;
import com.superinka.formex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/api/me")
    public User me(@AuthenticationPrincipal Jwt jwt) {
        String auth0Id = jwt.getSubject();
        String email = getClaim(jwt, "email");

        // 1. Buscar por Auth0 ID (Ideal)
        Optional<User> userByAuth0Id = userRepository.findByAuth0Id(auth0Id);
        if (userByAuth0Id.isPresent()) {
            return userByAuth0Id.get();
        }

        // 2. Si no existe por ID, buscar por Email (Caso de migración o admin pre-creado)
        if (email != null) {
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                // ¡ENCONTRADO! Actualizamos su auth0_id para vincularlo
                User existingUser = userByEmail.get();
                existingUser.setAuth0Id(auth0Id);
                // Opcional: Actualizar foto si viene nueva
                String picture = getClaim(jwt, "picture");
                if (picture != null) existingUser.setAvatarUrl(picture);

                return userRepository.save(existingUser);
            }
        }

        // 3. Si no existe ni por ID ni por Email, crear nuevo
        return crearUsuarioDesdeAuth0(jwt, auth0Id, email);
    }

    private User crearUsuarioDesdeAuth0(Jwt jwt, String auth0Id, String email) {
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("ROLE_STUDENT no existe"));

        String firstName = getClaim(jwt, "given_name");
        String lastName = getClaim(jwt, "family_name");
        String fullName = getClaim(jwt, "name");
        String picture = getClaim(jwt, "picture");

        if (firstName == null || lastName == null) {
            if (fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split(" ", 2);
                if (firstName == null) firstName = parts[0];
                if (lastName == null && parts.length > 1) lastName = parts[1];
            }
            if (firstName == null && email != null && fullName != null && fullName.equals(email)) {
                String nickname = getClaim(jwt, "nickname");
                if (nickname != null) firstName = nickname;
            }
        }

        if (firstName == null || firstName.isBlank()) firstName = "Usuario";
        if (lastName == null || lastName.isBlank()) lastName = ".";

        User user = User.builder()
                .auth0Id(auth0Id)
                .email(email != null ? email : auth0Id)
                .name(firstName)
                .lastname(lastName)
                .avatarUrl(picture)
                .enabled(true)
                .points(0)
                .teachingHours(0)
                .roles(Set.of(studentRole))
                .build();

        return userRepository.save(user);
    }

    private String getClaim(Jwt jwt, String claimName) {
        if (jwt.hasClaim(claimName)) {
            return jwt.getClaimAsString(claimName);
        }
        String customKey = "https://formex.com/" + claimName;
        if (jwt.hasClaim(customKey)) {
            return jwt.getClaimAsString(customKey);
        }
        return null;
    }
}
