package com.solea.web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_pedido")
public class ProductoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prenda_id")
    private Prenda prenda;

    private int cantidad;

    // precio de la prenda al momento del pedido
    private Double precioUnitario;

    // subtotal = cantidad * precioUnitario
    private Double subtotal;

    public void calcularSubtotal() {
        if (precioUnitario != null) {
            this.subtotal = precioUnitario * cantidad;
        }
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Prenda getPrenda() {
        return prenda;
    }

    public void setPrenda(Prenda prenda) {
        this.prenda = prenda;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
