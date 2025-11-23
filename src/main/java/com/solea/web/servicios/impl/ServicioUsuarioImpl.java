package com.solea.web.servicios.impl;

import com.solea.web.model.Rol;
import com.solea.web.model.Usuario;
import com.solea.web.model.UsuarioDetalleResponse;
import com.solea.web.repositorios.UsuarioRepository;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioUsuarioImpl implements ServicioUsuarios {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ServicioUsuarioImpl(UsuarioRepository usuarioRepository,
                               BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================================
    // REGISTRO
    // ================================
    @Override
    public void registarUsuario(Usuario u) {
        if (usuarioRepository.findByEmail(u.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Cifrar contraseña con BCrypt
        u.setPass(passwordEncoder.encode(u.getPass()));

        usuarioRepository.save(u);
    }

    // ================================
    // LOGIN (Ya no se usa con Spring Security)
    // Pero lo dejo funcionando para API o pruebas
    // ================================
    @Override
    public Usuario obtenerUserPorMailYpass(String email, String pass) {

        Optional<Usuario> opt = usuarioRepository.findByEmail(email);

        if (opt.isEmpty()) return null;

        Usuario u = opt.get();

        // Comprobar correctamente con BCrypt
        if (!passwordEncoder.matches(pass, u.getPass())) {
            return null;
        }

        return u;
    }

    // ================================
    // BÚSQUEDAS
    // ================================
    @Override
    public Usuario obtenerUserPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Usuario obtenerUserPorId(int id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    // ================================
    // EDITAR USUARIO
    // ================================
    @Override
    public void actualizarDatos(Integer id, String nombreUsuario, String pass, String telefono, String pais) {

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setNombre(nombreUsuario);
        u.setTel(telefono);
        u.setPais(pais);

        // Si el usuario desea cambiar contraseña
        if (pass != null && !pass.isBlank()) {
            u.setPass(passwordEncoder.encode(pass));
        }

        usuarioRepository.save(u);
    }

    // ================================
    // MÉTODOS WEB
    // ================================
    @Override
    public UsuarioDetalleResponse nativeObtenerUserPorId(int id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UsuarioDetalleResponse response = new UsuarioDetalleResponse();
        response.setId(u.getId());
        response.setNombre(u.getNombre());
        response.setEmail(u.getEmail());
        response.setTel(u.getTel());
        response.setPais(u.getPais());
        response.setRol(u.getRol().name());

        return response;
    }

    @Override
    public void guardarCambiosUsuario(Usuario usuarioEditar) {

        // Si la contraseña NO está encriptada, la encripto
        if (!usuarioEditar.getPass().startsWith("$2a$")) {
            usuarioEditar.setPass(passwordEncoder.encode(usuarioEditar.getPass()));
        }

        usuarioRepository.save(usuarioEditar);
    }

    // ================================
    // CAMBIAR ROL
    // ================================
    @Override
    public void cambiarRolUsuario(Integer id, Rol nuevoRol) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Evitar dejar el sistema sin ADMIN
        if (u.getRol() == Rol.ADMIN && nuevoRol != Rol.ADMIN) {
            if (esUltimoAdmin(id)) {
                throw new RuntimeException("No puedes quitar el rol ADMIN al último administrador");
            }
        }

        u.setRol(nuevoRol);
        usuarioRepository.save(u);
    }

    // ================================
    // VERIFICAR ÚLTIMO ADMIN
    // ================================
    @Override
    public boolean esUltimoAdmin(Integer idUsuario) {
        long admins = usuarioRepository.countByRol(Rol.ADMIN);

        if (admins <= 1) {
            Usuario u = usuarioRepository.findById(idUsuario).orElse(null);
            return u != null && u.getRol() == Rol.ADMIN;
        }

        return false;
    }
}
