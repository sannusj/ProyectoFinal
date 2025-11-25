package com.solea.web.controladores;

import com.solea.web.model.Usuario;
import com.solea.web.repositorios.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
    name = "Usuario", 
    description = "API REST para recursos relacionados con usuarios. " +
                  "Proporciona endpoints para obtener recursos multimedia (avatares) de usuarios. " +
                  "Los recursos se sirven como archivos binarios con tipos MIME apropiados."
)
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(
            summary = "Obtener imagen de avatar de usuario",
            description = "Devuelve la imagen de avatar/foto de perfil de un usuario específico identificado por su ID. " +
                         "La imagen se devuelve como array de bytes con Content-Type image/png y headers apropiados. " +
                         "Si el usuario no existe o no tiene avatar asignado, retorna 404 NOT FOUND. " +
                         "Este endpoint es público para permitir visualización de avatares en la interfaz. " +
                         "La imagen se almacena en la base de datos como BLOB y se sirve directamente desde ahí."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Avatar encontrado y devuelto exitosamente como imagen PNG con headers correctos",
                    content = @Content(mediaType = "image/png")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado o usuario sin avatar asignado",
                    content = @Content
            )
    })
    @GetMapping("/usuario/avatar/{id}")
    public ResponseEntity<byte[]> avatar(
            @Parameter(
                    description = "ID único del usuario en el sistema", 
                    required = true, 
                    example = "1"
            )
            @PathVariable int id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null || u.getAvatar() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        byte[] img = u.getAvatar();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(img.length);
        return new ResponseEntity<>(img, headers, HttpStatus.OK);
    }
}

