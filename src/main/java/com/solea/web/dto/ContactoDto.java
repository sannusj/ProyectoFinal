package com.solea.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos del formulario de contacto")
public class ContactoDto {

    @Schema(
            description = "Nombre completo del remitente",
            example = "María González López",
            required = true
    )
    private String nombre;

    @Schema(
            description = "Correo electrónico del remitente",
            example = "maria.gonzalez@ejemplo.com",
            required = true,
            format = "email"
    )
    private String email;

    @Schema(
            description = "Asunto o tema del mensaje",
            example = "Consulta sobre devolución de producto",
            required = true
    )
    private String asunto;

    @Schema(
            description = "Contenido del mensaje o consulta",
            example = "Buenos días, quisiera saber cuál es el proceso para realizar una devolución. Compré una prenda hace 5 días y me gustaría cambiarla por otra talla. ¿Qué documentos necesito?",
            required = true
    )
    private String mensaje;

    public ContactoDto() {
    }

    public ContactoDto(String nombre, String email, String asunto, String mensaje) {
        this.nombre = nombre;
        this.email = email;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
