package com.solea.web.servicios;

import java.util.List;
import java.util.Map;

import com.solea.web.model.Categoria;

public interface ServicioCategorias {
    List<Categoria> obtenerCategorias();
    Map<String, String> obtenerCategoriasParaDesplegable();
    void registrarCategoria(Categoria c);
    Categoria obtenerCategoriaPorId(Integer id);
    void guardarCambiosCategoria(Categoria categoriaEditar);
    void eliminarCategoria(Integer id);
}
