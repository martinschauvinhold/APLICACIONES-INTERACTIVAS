package com.uade.tpo.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.demo.exceptions.BusinessRuleException;

/**
 * Guarda imágenes subidas en disco y devuelve su URL pública. Valida
 * content-type (solo JPEG/PNG/WEBP) y tamaño, y genera un nombre único por
 * archivo para no pisar uploads previos.
 */
@Service
public class StorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    /** Content-type permitido -> extensión del archivo guardado. */
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path root;
    private final String baseUrl;
    private final String urlPath;

    public StorageService(
            @Value("${app.uploads.dir}") String uploadsDir,
            @Value("${app.uploads.base-url}") String baseUrl,
            @Value("${app.uploads.url-path}") String urlPath) {
        this.root = Paths.get(uploadsDir).toAbsolutePath().normalize();
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.urlPath = urlPath;
    }

    /** Persiste el archivo y devuelve su URL pública absoluta. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("El archivo de imagen está vacío");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessRuleException("La imagen supera el máximo de 5MB");
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessRuleException("Tipo de imagen no permitido (solo JPEG, PNG o WEBP)");
        }

        String filename = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(root);
            Path target = root.resolve(filename).normalize();
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessRuleException("No se pudo guardar la imagen");
        }
        return baseUrl + urlPath + "/" + filename;
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
