package com.solea.web.dto;

import java.util.Base64;

public class ProductoCarritoDto {

    private Integer prendaId;
    private String nombre;
    private Double precio;
    private int cantidad;
    private byte[] imagenPrenda;
    private String imagePath; // nueva propiedad para ruta de fichero

    public ProductoCarritoDto(Integer prendaId, String nombre, Double precio, int cantidad, byte[] imagenPrenda, String imagePath) {
        this.prendaId = prendaId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imagenPrenda = imagenPrenda;
        this.imagePath = imagePath;
    }

    public Integer getPrendaId() { return prendaId; }
    public String getNombre() { return nombre; }
    public Double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public byte[] getImagenPrenda() { return imagenPrenda; }
    public String getImagePath() { return imagePath; }

    // Compatibilidad con plantillas que esperan 'id' e 'imagen' y 'subtotal'
    public Integer getId() { return prendaId; }

    public String getImagen() {
        // Priorizar BLOB en BD
        if (imagenPrenda != null && imagenPrenda.length > 0) {
            String base64 = Base64.getEncoder().encodeToString(imagenPrenda);
            // asumir PNG/ JPEG no disponible; usar generic image/png
            return "data:image/png;base64," + base64;
        }

        // Si no hay BLOB, devolver la URL del endpoint que sirve la imagen por id.
        // El controlador /imagenes/{id} prioriza file system (imagePath) y hace fallback a BLOB.
        return "/imagenes/" + prendaId;
    }

    public Double getSubtotal() {
        return (precio != null ? precio : 0.0) * cantidad;
    }
}
