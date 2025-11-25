package com.solea.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de envío para el pedido - Paso 1 del checkout")
public class DatosEnvioDto {

    @Schema(
            description = "Nombre completo del destinatario",
            example = "Juan Pérez García",
            required = true
    )
    private String nombre;

    @Schema(
            description = "Dirección completa de envío",
            example = "Calle Principal 123, Apto 4B, Edificio Torres del Centro",
            required = true
    )
    private String direccion;

    @Schema(
            description = "Provincia, estado o departamento",
            example = "Bogotá D.C.",
            required = true
    )
    private String provincia;

    public DatosEnvioDto() {
    }

    public DatosEnvioDto(String nombre, String direccion, String provincia) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.provincia = provincia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }
}
