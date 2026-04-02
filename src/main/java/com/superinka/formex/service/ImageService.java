package com.superinka.formex.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "courses")
            );
            return uploadResult.get("secure_url").toString(); // 🔥 URL pública
        } catch (Exception e) {
            e.printStackTrace(); // 🔥 VER ERROR REAL EN LOGS
            throw new RuntimeException("Error subiendo imagen");
        }
    }
}

