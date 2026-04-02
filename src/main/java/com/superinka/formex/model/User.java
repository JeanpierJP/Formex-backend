package com.superinka.formex.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CAMBIO: Separamos fullName en name y lastname para mejor gestión
    // Si tu DB ya tiene datos en 'full_name', podrías necesitar una migración o mantenerlo
    // Para este caso, adaptamos a lo que pide el controlador:

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String lastname;

    @NotBlank
    @Size(max = 100)
    private String email;

    @Column(nullable = true)
    @Size(max = 255)
    private String password;

    @Size(max = 20)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "auth0_id", unique = true)
    private String auth0Id;

    // 🔥 PUNTOS DEL ALUMNO
    @Builder.Default
    @Column(nullable = false)
    private Integer points = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "teaching_hours", nullable = false)
    @Builder.Default
    private Integer teachingHours = 0;

    // Constructor específico usado por AdminUserController
    public User(String name, String lastname, String email, String password) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    // Metodo helper para obtener nombre completo si lo necesitas en otras partes
    public String getFullName() {
        return this.name + " " + this.lastname;
    }
}
