package com.solea.web.dto;

import java.util.Base64;

public class ProductoCarritoDto {

    private Integer prendaId;
    private String nombre;
    private Double precio;
    private int cantidad;
    private byte[] imagenPrenda;

    public ProductoCarritoDto(Integer prendaId, String nombre, Double precio, int cantidad, byte[] imagenPrenda) {
        this.prendaId = prendaId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imagenPrenda = imagenPrenda;
    }

    public Integer getPrendaId() { return prendaId; }
    public String getNombre() { return nombre; }
    public Double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public byte[] getImagenPrenda() { return imagenPrenda; }

    // Compatibilidad con plantillas que esperan 'id' e 'imagen' y 'subtotal'
    public Integer getId() { return prendaId; }

    public String getImagen() {
        if (imagenPrenda == null) return null;
        String base64 = Base64.getEncoder().encodeToString(imagenPrenda);
        // asumir PNG/ JPEG no disponible; usar generic image/png
        return "data:image/png;base64," + base64;
    }

    public Double getSubtotal() {
        return (precio != null ? precio : 0.0) * cantidad;
    }
}
