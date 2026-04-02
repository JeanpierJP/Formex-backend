package com.superinka.formex.config;

import com.superinka.formex.model.Category;
import com.superinka.formex.model.Role;
import com.superinka.formex.model.User;
import com.superinka.formex.model.enums.RoleName;
import com.superinka.formex.repository.CategoryRepository;
import com.superinka.formex.repository.RoleRepository;
import com.superinka.formex.repository.UserRepository;
import com.superinka.formex.service.Auth0Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository; // Inyectamos repositorio de categorías
    private final PasswordEncoder passwordEncoder;
    private final Auth0Service auth0Service;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Iniciando carga de datos semilla...");

        // 1. Crear Roles si no existen
        createRoleIfNotFound(RoleName.ROLE_ADMIN);
        createRoleIfNotFound(RoleName.ROLE_INSTRUCTOR);
        createRoleIfNotFound(RoleName.ROLE_STUDENT);
        System.out.println("✅ Roles verificados/creados.");

        // 2. Crear Categorías por defecto si no existen
        if (categoryRepository.count() == 0) {
            List<String> defaultCategories = List.of(
                    "Desarrollo de Software",
                    "Diseño Gráfico",
                    "Marketing Digital",
                    "Negocios y Emprendimiento",
                    "Idiomas",
                    "Desarrollo Personal",
                    "Fotografía y Video",
                    "Música"
            );

            for (String catName : defaultCategories) {
                Category category = new Category();
                category.setName(catName);
                category.setDescription("Cursos relacionados con " + catName);
                categoryRepository.save(category);
            }
            System.out.println("✅ Categorías iniciales creadas.");
        }

        // 3. Crear Admin por defecto si no existe
        if (!userRepository.existsByEmail("admin@formex.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setLastname("Principal");
            admin.setEmail("admin@formex.com");

            // Contraseña fuerte para cumplir políticas de Auth0
            String rawPassword = "Admin123$Secure!";

            admin.setPassword(passwordEncoder.encode(rawPassword));
            admin.setEnabled(true);

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role Admin no encontrado."));
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado en DB Local: admin@formex.com");

            // 4. Sincronizar Admin con Auth0
            try {
                auth0Service.createAuth0User(
                        admin.getEmail(),
                        rawPassword,
                        admin.getName() + " " + admin.getLastname(),
                        RoleName.ROLE_ADMIN.name() // Enviamos el rol para que se guarde en app_metadata
                );
                System.out.println("✅ Sincronización Auth0: Admin enviado a la nube.");
            } catch (Exception e) {
                System.err.println("⚠️ Advertencia: No se pudo crear Admin en Auth0 (Verificar credenciales M2M): " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ El usuario Admin ya existe.");
        }

        System.out.println("🚀 Carga de datos completada.");
    }

    private void createRoleIfNotFound(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}
