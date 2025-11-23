package com.solea.web.config;

import com.solea.web.model.Usuario;
import com.solea.web.model.Rol;
import com.solea.web.servicios.ServicioUsuarios;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final ServicioUsuarios servicioUsuarios;

    public DataInitializer(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @PostConstruct
    public void init() {
        try {
            final String adminEmail = "admin@solea.com";

            Usuario admin = servicioUsuarios.obtenerUserPorEmail(adminEmail);

            if (admin == null) {
                crearAdmin(adminEmail);
                return;
            }

            asegurarAdmin(admin);

        } catch (Exception ex) {
            log.error("Error inicializando datos del sistema", ex);
        }
    }

    private void crearAdmin(String adminEmail) {
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail(adminEmail);
        admin.setPass("admin123"); // El servicio se encarga de encriptar
        admin.setRol(Rol.ADMIN);
        admin.setTel("0000000000");
        admin.setPais("Colombia");

        servicioUsuarios.registarUsuario(admin);

        log.info(">>> ADMIN creado automáticamente: {} (contraseña: admin123)", adminEmail);
    }

    private void asegurarAdmin(Usuario existente) {

        boolean changed = false;

        // Asegurar rol ADMIN
        if (existente.getRol() != Rol.ADMIN) {
            servicioUsuarios.cambiarRolUsuario(existente.getId(), Rol.ADMIN);
            log.info("Rol de usuario '{}' actualizado a ADMIN", existente.getEmail());
            changed = true;
        }

        // Asegurar contraseña en Bcrypt
        if (!esBcrypt(existente.getPass())) {
            existente.setPass("admin123");
            servicioUsuarios.guardarCambiosUsuario(existente);
            log.info("Contraseña de '{}' fue reestablecida y encriptada", existente.getEmail());
            changed = true;
        }

        if (!changed) {
            log.info("Admin existe y está correctamente configurado: {}", existente.getEmail());
        }
    }

    private boolean esBcrypt(String pass) {
        return pass != null && (
                pass.startsWith("$2a$") ||
                        pass.startsWith("$2b$") ||
                        pass.startsWith("$2y$")
        );
    }
}
