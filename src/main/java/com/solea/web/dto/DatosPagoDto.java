package com.solea.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de pago con tarjeta - Paso 2 del checkout")
public class DatosPagoDto {

    @Schema(
            description = "Nombre del titular de la tarjeta",
            example = "Juan Pérez García",
            required = true
    )
    private String titular;

    @Schema(
            description = "Número de tarjeta de crédito/débito (16 dígitos)",
            example = "4111111111111111",
            required = true,
            pattern = "\\d{16}"
    )
    private String numero;

    @Schema(
            description = "Tipo de tarjeta",
            example = "Visa",
            required = true,
            allowableValues = {"Visa", "MasterCard", "American Express", "Diners Club"}
    )
    private String tipoTarjeta;

    public DatosPagoDto() {
    }

    public DatosPagoDto(String titular, String numero, String tipoTarjeta) {
        this.titular = titular;
        this.numero = numero;
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }
}
