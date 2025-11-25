package com.solea.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para editar el perfil del usuario")
public class EditarPerfilDto {

    @Schema(
            description = "Nuevo nombre del usuario",
            example = "Juan Pérez García",
            required = true
    )
    private String nombre;

    @Schema(
            description = "Nuevo teléfono del usuario",
            example = "+57 300 123 4567",
            required = true
    )
    private String telefono;

    @Schema(
            description = "Nuevo país del usuario",
            example = "Colombia",
            required = true
    )
    private String pais;

    public EditarPerfilDto() {
    }

    public EditarPerfilDto(String nombre, String telefono, String pais) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.pais = pais;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }
}
