package com.superinka.formex.repository;

import com.superinka.formex.model.User;
import com.superinka.formex.model.enums.RoleName;
import com.superinka.formex.payload.response.StudentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByAuth0Id(String auth0Id);

    Boolean existsByEmail(String email);

    // ✅ NUEVO: Metodo para buscar usuarios por su Rol
    // Spring Data JPA interpreta esto como: JOIN con tabla roles y filtrar por nombre
    List<User> findByRoles_Name(RoleName name);


    // ✅ 1️⃣ Contar estudiantes activos
    @Query("""
        SELECT COUNT(u)
        FROM User u
        JOIN u.roles r
        WHERE r.name = com.superinka.formex.model.enums.RoleName.ROLE_STUDENT
        AND u.enabled = true
    """)
    Long countActiveStudents();

    @Query("""
    SELECT new com.superinka.formex.payload.response.StudentDto(
        u.id,
        CONCAT(u.name, ' ', u.lastname),
        u.email,
        u.phone,
        uc.paymentStatus,
        0.0,
        COALESCE(u.points, 0)
    )
    FROM UserCourse uc
    JOIN uc.user u
    WHERE uc.course.id = :courseId
""")
    List<StudentDto> findStudentsByCourseId(@Param("courseId") Long courseId);
}
