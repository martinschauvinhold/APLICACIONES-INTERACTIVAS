package com.uade.tpo.demo.config;

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
        String location = Paths.get(uploadsDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(urlPath + "/**")
                .addResourceLocations(location);
    }
}
