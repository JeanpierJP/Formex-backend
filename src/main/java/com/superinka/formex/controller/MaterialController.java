package com.superinka.formex.controller;

import com.superinka.formex.model.Material;
import com.superinka.formex.payload.request.MaterialRequest;
import com.superinka.formex.service.MaterialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions/{sessionId}/materials")
@PreAuthorize("hasRole('INSTRUCTOR') or hasRole('STUDENT')")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * LISTAR materiales de una sesión
     * GET /api/sessions/{sessionId}/materials
     */
    @GetMapping
    public ResponseEntity<List<Material>> getMaterials(
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(materialService.getBySession(sessionId));
    }

    /**
     * CREAR material para una sesión
     * POST /api/sessions/{sessionId}/materials
     */
    @PostMapping
    public ResponseEntity<Material> createMaterial(
            @PathVariable Long sessionId,
            @RequestBody MaterialRequest request
    ) {
        return ResponseEntity.ok(materialService.create(sessionId, request));
    }

    /**
     * ELIMINAR material
     * DELETE /api/materials/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.ok().build();
    }
}
