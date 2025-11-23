package com.solea.web.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoCarrito> productosCarrito = new ArrayList<>();

    public Carrito() {}

    public Carrito(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getId() { return id; }

    public Usuario getUsuario() { return usuario; }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<ProductoCarrito> getProductosCarrito() { return productosCarrito; }

    public void setProductosCarrito(List<ProductoCarrito> productosCarrito) {
        this.productosCarrito = productosCarrito;
    }

    public void addProducto(ProductoCarrito pc) {
        productosCarrito.add(pc);
        pc.setCarrito(this);
    }

    public void removeProducto(ProductoCarrito pc) {
        productosCarrito.remove(pc);
        pc.setCarrito(null);
    }
}
