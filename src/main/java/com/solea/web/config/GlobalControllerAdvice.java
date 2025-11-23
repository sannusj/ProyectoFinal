package com.solea.web.config;

import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final ServicioUsuarios servicioUsuarios;

    public GlobalControllerAdvice(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @ModelAttribute("usuario")
    public Usuario addUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return servicioUsuarios.obtenerUserPorEmail(auth.getName());
    }
}

