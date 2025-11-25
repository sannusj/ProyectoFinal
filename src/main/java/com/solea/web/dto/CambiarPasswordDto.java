package com.solea.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para cambiar la contraseña del usuario")
public class CambiarPasswordDto {

    @Schema(
            description = "Contraseña actual del usuario (para verificación)",
            example = "MiPasswordActual123",
            required = true,
            format = "password"
    )
    private String actual;

    @Schema(
            description = "Nueva contraseña deseada",
            example = "MiNuevaPassword456!",
            required = true,
            format = "password",
            minLength = 6
    )
    private String nueva;

    @Schema(
            description = "Confirmación de la nueva contraseña (debe coincidir con 'nueva')",
            example = "MiNuevaPassword456!",
            required = true,
            format = "password"
    )
    private String repetir;

    public CambiarPasswordDto() {
    }

    public CambiarPasswordDto(String actual, String nueva, String repetir) {
        this.actual = actual;
        this.nueva = nueva;
        this.repetir = repetir;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getNueva() {
        return nueva;
    }

    public void setNueva(String nueva) {
        this.nueva = nueva;
    }

    public String getRepetir() {
        return repetir;
    }

    public void setRepetir(String repetir) {
        this.repetir = repetir;
    }
}
