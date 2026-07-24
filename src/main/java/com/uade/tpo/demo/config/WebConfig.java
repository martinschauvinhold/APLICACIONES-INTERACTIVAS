package com.uade.tpo.demo.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sirve la carpeta de uploads como contenido estático: una request a
 * {@code <url-path>/<archivo>} devuelve el archivo en {@code app.uploads.dir}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.dir:uploads/products}")
    private String uploadsDir;

    @Value("${app.uploads.url-path:/uploads/products}")
    private String urlPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        // Crear la carpeta si aún no existe: si no, Path.toUri() no agrega la barra
        // final y Spring trata la ubicación como un archivo, devolviendo 404 en las
        // imágenes hasta el próximo reinicio (p. ej. tras un arranque limpio o wipe-db).
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
            // Si falla, StorageService fallará al escribir con un error más claro.
        }
        String location = dir.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler(urlPath + "/**")
                .addResourceLocations(location);
    }
}
