package com.superinka.formex.service;

import com.superinka.formex.model.Material;
import com.superinka.formex.model.Session;
import com.superinka.formex.payload.request.MaterialRequest;
import com.superinka.formex.repository.MaterialRepository;
import com.superinka.formex.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final SessionRepository sessionRepository;

    public MaterialService(MaterialRepository materialRepository,
                           SessionRepository sessionRepository) {
        this.materialRepository = materialRepository;
        this.sessionRepository = sessionRepository;
    }

    // 📌 LISTAR materiales de una sesión
    public List<Material> getBySession(Long sessionId) {
        return materialRepository.findBySession_Id(sessionId);
    }

    // 📌 CREAR material SOLO PARA UNA SESIÓN
    public Material create(Long sessionId, MaterialRequest request) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        Material material = new Material();
        material.setTitle(request.getTitle());
        material.setDescription(request.getDescription());
        material.setLink(request.getLink());
        material.setSession(session);

        return materialRepository.save(material);
    }

    // 📌 ELIMINAR material (solo ese, no afecta otras sesiones)
    public void delete(Long materialId) {
        materialRepository.deleteById(materialId);
    }
}
