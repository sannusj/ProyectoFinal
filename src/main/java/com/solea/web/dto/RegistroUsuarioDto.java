package com.solea.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para el registro de un nuevo usuario en el sistema")
public class RegistroUsuarioDto {

    @Schema(
            description = "Nombre completo del usuario",
            example = "Juan Pérez García",
            required = true,
            minLength = 3,
            maxLength = 100
    )
    private String nombre;

    @Schema(
            description = "Correo electrónico del usuario (será usado como nombre de usuario)",
            example = "juan.perez@ejemplo.com",
            required = true,
            format = "email"
    )
    private String email;

    @Schema(
            description = "Contraseña del usuario (será encriptada con BCrypt)",
            example = "MiPassword123!",
            required = true,
            minLength = 6,
            format = "password"
    )
    private String pass;

    @Schema(
            description = "Número de teléfono del usuario",
            example = "+57 300 123 4567",
            required = false
    )
    private String tel;

    @Schema(
            description = "País de residencia del usuario",
            example = "Colombia",
            required = false
    )
    private String pais;

    // Constructores
    public RegistroUsuarioDto() {
    }

    public RegistroUsuarioDto(String nombre, String email, String pass, String tel, String pais) {
        this.nombre = nombre;
        this.email = email;
        this.pass = pass;
        this.tel = tel;
        this.pais = pais;
    }

    // Getters y Setters
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

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }
}
