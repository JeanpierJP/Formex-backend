package com.superinka.formex.repository;

import com.superinka.formex.model.Role;
import com.superinka.formex.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    //Necesario para asignar el rol por defecto (ROLE_STUDENT) al registrarse
    Optional<Role> findByName(RoleName name);
}
