package com.superinka.formex.payload.request;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    private String name;
    private String lastname;

    private String phone;
    private String email;

    // CAMBIO: Simplificamos roles si queremos actualizar el rol principal
    // O lo dejamos opcional si solo actualizamos perfil
    private String role;

    private Boolean enabled;

    // Opcional: si necesitas asignar curso al actualizar
    private Long courseId;
}
