package com.solea.web.datos.serviciosWeb;

import java.util.List;

public class ResumenPedido {

    private String nombre;
    private String direccion;
    private String provincia;
    private String observaciones;
    private boolean regalo;
    private List<ProductoResumen> productos;
    private double total;

    public ResumenPedido() {}

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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isRegalo() {
        return regalo;
    }

    public void setRegalo(boolean regalo) {
        this.regalo = regalo;
    }

    public List<ProductoResumen> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoResumen> productos) {
        this.productos = productos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public static class ProductoResumen {
        private String nombre;
        private int cantidad;
        private Double precioUnitario;
        private Double subtotal;

        public ProductoResumen(String nombre, int cantidad, Double precioUnitario, Double subtotal) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }

        public String getNombre() {
            return nombre;
        }

        public int getCantidad() {
            return cantidad;
        }

        public Double getPrecioUnitario() {
            return precioUnitario;
        }

        public Double getSubtotal() {
            return subtotal;
        }
    }
}

