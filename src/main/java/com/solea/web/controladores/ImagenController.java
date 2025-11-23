package com.solea.web.controladores;

import com.solea.web.model.Prenda;
import com.solea.web.repositorios.PrendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ImagenController {

    private static final Logger logger = LoggerFactory.getLogger(ImagenController.class);
    private final PrendaRepository prendaRepository;

    public ImagenController(PrendaRepository prendaRepository) {
        this.prendaRepository = prendaRepository;
    }

    @GetMapping("/imagenes/{id}")
    public ResponseEntity<byte[]> imagenPrenda(@PathVariable int id) {
        Prenda p = prendaRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            // Priorizar imagePath en disco
            if (p.getImagePath() != null && !p.getImagePath().isBlank()) {
                Path ruta = Paths.get(p.getImagePath());

                // Si la ruta es relativa (no comienza con drive o /), relativizamos respecto al directorio del proyecto
                if (!ruta.isAbsolute()) {
                    ruta = Paths.get(System.getProperty("user.dir")).resolve(p.getImagePath());
                }

                if (Files.exists(ruta)) {
                    byte[] contenido = Files.readAllBytes(ruta);
                    String tipo = Files.probeContentType(ruta);
                    MediaType mediaType = MediaType.IMAGE_PNG;
                    if (tipo != null) {
                        try {
                            mediaType = MediaType.parseMediaType(tipo);
                        } catch (Exception e) {
                            logger.warn("No se pudo parsear content-type: {}", tipo);
                        }
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(mediaType);
                    headers.setContentLength(contenido.length);
                    return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
                }
            }

            // Fallback: blob en BD
            if (p.getImagenPrenda() != null) {
                byte[] img = p.getImagenPrenda();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(img.length);
                return new ResponseEntity<>(img, headers, HttpStatus.OK);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (IOException e) {
            logger.error("Error leyendo imagen de prenda id={}", id, e);
            // Devolver 404 en lugar de 500 para no romper el render de páginas que incluyen imágenes
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
