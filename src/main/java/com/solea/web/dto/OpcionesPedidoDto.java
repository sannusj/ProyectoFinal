package com.solea.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Opciones adicionales del pedido - Paso 3 del checkout")
public class OpcionesPedidoDto {

    @Schema(
            description = "Indica si el pedido es para regalo",
            example = "si",
            required = false,
            allowableValues = {"si", "no", "true", "false", "on"}
    )
    private String regalo;

    @Schema(
            description = "Observaciones o instrucciones especiales de entrega",
            example = "Por favor entregar después de las 2pm. Dejar con el portero si no hay nadie en casa.",
            required = false
    )
    private String observaciones;

    public OpcionesPedidoDto() {
    }

    public OpcionesPedidoDto(String regalo, String observaciones) {
        this.regalo = regalo;
        this.observaciones = observaciones;
    }

    public String getRegalo() {
        return regalo;
    }

    public void setRegalo(String regalo) {
        this.regalo = regalo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
