package com.superinka.formex.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final Path fileStorageLocation;

    public MediaController(){
        //Define la carpeta donde se guardaran las imagenes
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio de subida." , ex);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file")MultipartFile file) {
        //Validar si el archivo está vacio
        if(file.isEmpty()){
            throw new RuntimeException("Error: Archivo vacio.");
        }

        try{
            //Generar nombre unico para evitar problemas
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

            //Guardar el archivo
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            //Generar la URL pública para acceder a la imagen
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/images/")
                    .path(uniqueFileName)
                    .toUriString();

            //Devolver la URL en un JSON
            Map<String, String> response = new HashMap<>();
            response.put("url", fileDownloadUri);

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo" + file.getOriginalFilename(), ex);
        }
    }
}
