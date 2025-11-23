package com.solea.web.controladores;

import com.solea.web.model.Usuario;
import com.solea.web.repositorios.UsuarioRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/usuario/avatar/{id}")
    public ResponseEntity<byte[]> avatar(@PathVariable int id) {
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

