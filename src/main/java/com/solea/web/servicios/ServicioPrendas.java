package com.solea.web.servicios;

import com.solea.web.model.Prenda;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ServicioPrendas {

    // CRUD moderno
    List<Prenda> obtenerTodas();
    Prenda obtenerPorId(int id);
    Prenda registrarPrenda(Prenda prenda);
    Prenda guardarCambiosPrenda(Prenda prenda);
    void eliminarPrenda(int id);

    // Imagen
    void guardarImagen(int idPrenda, MultipartFile archivo);

    // Catálogo
    List<Prenda> buscarPorNombre(String nombre);
}
