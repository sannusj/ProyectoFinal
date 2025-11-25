package com.solea.web.controladores;

import com.solea.web.model.Prenda;
import com.solea.web.repositorios.PrendaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Imagenes", 
    description = "API REST para servir imágenes de productos/prendas del catálogo. " +
                  "Implementa sistema híbrido de almacenamiento: busca primero en sistema de archivos (imagePath), " +
                  "si no encuentra, busca en base de datos (BLOB). " +
                  "Detecta automáticamente el tipo MIME de la imagen y configura headers HTTP apropiados. " +
                  "Maneja errores devolviendo 404 si la prenda o imagen no existen."
)
public class ImagenController {

    private static final Logger logger = LoggerFactory.getLogger(ImagenController.class);
    private final PrendaRepository prendaRepository;

    public ImagenController(PrendaRepository prendaRepository) {
        this.prendaRepository = prendaRepository;
    }

    @Operation(
            summary = "Obtener imagen de prenda/producto del catálogo",
            description = "Devuelve la imagen de una prenda identificada por su ID con estrategia de almacenamiento híbrida: \n" +
                         "1. **Prioridad al sistema de archivos**: Si la prenda tiene `imagePath` configurado, busca el archivo en disco. " +
                         "   Soporta rutas absolutas y relativas (se resuelven desde el directorio del proyecto). \n" +
                         "2. **Fallback a base de datos**: Si no existe imagePath o el archivo no se encuentra, " +
                         "   busca la imagen almacenada como BLOB en el campo `imagenPrenda`. \n" +
                         "3. **Detección automática de tipo MIME**: Utiliza `Files.probeContentType()` para determinar " +
                         "   el Content-Type correcto (image/png, image/jpeg, etc.). \n" +
                         "4. **Manejo de errores**: Si la prenda no existe o no tiene imagen, retorna 404. " +
                         "   Los errores de I/O también se manejan devolviendo 404 para no romper el render de páginas. \n" +
                         "Este endpoint es público para permitir la visualización de productos en el catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen encontrada y devuelta exitosamente con Content-Type detectado automáticamente",
                    content = @Content(mediaType = "image/png")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Prenda no encontrada, sin imagen asignada, o error al leer el archivo",
                    content = @Content
            )
    })
    @GetMapping("/imagenes/{id}")
    public ResponseEntity<byte[]> imagenPrenda(
            @Parameter(
                    description = "ID único de la prenda/producto en el sistema", 
                    required = true, 
                    example = "1"
            )
            @PathVariable int id) {
        Prenda p = prendaRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            // Priorizar imagePath en disco
            if (p.getImagePath() != null && !p.getImagePath().isBlank()) {
                Path ruta = Paths.get(p.getImagePath());

                // Si la ruta es relativa, relativizar respecto al directorio del proyecto
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
