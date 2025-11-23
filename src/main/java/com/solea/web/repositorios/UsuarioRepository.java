package com.solea.web.repositorios;

import com.solea.web.model.Usuario;
import com.solea.web.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRol(Rol rol);
    long countByRol(Rol rol);
}
