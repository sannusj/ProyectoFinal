package com.solea.web.servicios;

import java.util.List;
import java.util.Map;

import com.solea.web.model.Usuario;
import com.solea.web.model.Rol;
import com.solea.web.model.UsuarioDetalleResponse;

public interface ServicioUsuarios {


    void registarUsuario(Usuario u);

    Usuario obtenerUserPorMailYpass(String email, String pass);
    Usuario obtenerUserPorEmail(String email);
    Usuario obtenerUserPorId(int id);
    List<Usuario> obtenerUsuarios();
    void actualizarDatos(Integer id, String nombreUsuario, String pass, String telefono, String pais);

    // metodos web
    UsuarioDetalleResponse nativeObtenerUserPorId(int id);

    void guardarCambiosUsuario(Usuario usuarioEditar);

    void cambiarRolUsuario(Integer id, Rol nuevoRol);

    boolean esUltimoAdmin(Integer idUsuario);

    // Procesar login OAuth: crear o actualizar usuario según atributos del proveedor
    com.solea.web.model.Usuario processOAuthPostLogin(String providerName, java.util.Map<String, Object> attributes);
}
