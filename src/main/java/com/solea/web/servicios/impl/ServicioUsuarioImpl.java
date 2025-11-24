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

    // ================================
    // PROCESAR LOGIN OAUTH
    // ================================
    @Override
    public Usuario processOAuthPostLogin(String providerName, java.util.Map<String, Object> attributes) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServicioUsuarioImpl.class);
        log.info("processOAuthPostLogin provider={} attributesKeys={}", providerName, attributes != null ? attributes.keySet() : null);
         // Actualmente soportamos Google
         Usuario.Provider provider = Usuario.Provider.valueOf(providerName.toUpperCase());

         // Extraer oauthId: primero intentamos la clave normalizada que puso el servicio OAuth
        String oauthId = null;
        if (attributes.containsKey("oauth_id_normalized")) {
            oauthId = String.valueOf(attributes.get("oauth_id_normalized"));
        } else if (attributes.containsKey("sub")) {
            oauthId = String.valueOf(attributes.get("sub"));
        } else if (attributes.containsKey("id")) {
            oauthId = String.valueOf(attributes.get("id"));
        }

        String email = attributes.containsKey("email") ? String.valueOf(attributes.get("email")) : null;
        String name = attributes.containsKey("name") ? String.valueOf(attributes.get("name")) : null;

        // Si ya existe usuario por provider+oauthId -> actualizar
        if (oauthId != null) {
            java.util.Optional<Usuario> opt = usuarioRepository.findByProviderAndOauthId(provider, oauthId);
            if (opt.isPresent()) {
                Usuario u = opt.get();
                if (name != null && (u.getNombre() == null || u.getNombre().isBlank())) u.setNombre(name);
                // podríamos actualizar avatar/otros campos aquí
                return usuarioRepository.save(u);
            }
        }

        // Si no existe por oauthId, buscar por email
        if (email != null) {
            java.util.Optional<Usuario> byEmail = usuarioRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                Usuario existing = byEmail.get();
                // Si el usuario ya es de este provider, ligar
                if (existing.getProvider() == provider || existing.getProvider() == Usuario.Provider.LOCAL) {
                    existing.setProvider(provider);
                    if (oauthId != null) existing.setOauthId(oauthId);
                    if (existing.getRol() == null) existing.setRol(Rol.USER);
                    if (existing.getNombre() == null || existing.getNombre().isBlank()) existing.setNombre(name);
                    // Asegurar que la columna pass no sea nula (crear una contraseña aleatoria cifrada)
                    if (existing.getPass() == null || existing.getPass().isBlank()) {
                        existing.setPass(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    }
                    return usuarioRepository.save(existing);
                }
            }
        }

        // Si no existe, crear nuevo usuario
        Usuario nuevo = new Usuario();
        nuevo.setEmail(email);
        nuevo.setNombre(name);
        nuevo.setProvider(provider);
        nuevo.setOauthId(oauthId);
        nuevo.setRol(Rol.USER);
        // Establecer contraseña aleatoria cifrada para cumplir restricciones DB y evitar login por contraseña
        nuevo.setPass(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));

        return usuarioRepository.save(nuevo);
    }
}
