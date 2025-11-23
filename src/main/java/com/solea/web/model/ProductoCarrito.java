package com.solea.web.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "productos_carrito")
public class ProductoCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prenda_id")
    private Prenda prenda;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id")
    private Carrito carrito;

    private int cantidad;

    public ProductoCarrito() {}

    public ProductoCarrito(Prenda prenda, Carrito carrito, int cantidad) {
        this.prenda = prenda;
        this.carrito = carrito;
        this.cantidad = cantidad;
    }

    public Integer getId() { return id; }

    public Prenda getPrenda() { return prenda; }

    public void setPrenda(Prenda prenda) { this.prenda = prenda; }

    public Carrito getCarrito() { return carrito; }

    public void setCarrito(Carrito carrito) { this.carrito = carrito; }

    public int getCantidad() { return cantidad; }

    public void setCantidad(int cantidad) {
        this.cantidad = Math.max(1, cantidad);
    }
}
