package com.solea.web.servicios.impl;

import com.solea.web.model.Prenda;
import com.solea.web.repositorios.PrendaRepository;
import com.solea.web.servicios.ServicioPrendas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class ServicioPrendasImpl implements ServicioPrendas {

    private final PrendaRepository prendaRepository;

    private final Path uploadDir;
    private final long maxSize;
    private final List<String> allowedExt;

    public ServicioPrendasImpl(PrendaRepository prendaRepository,
                               @Value("${app.upload.dir}") String uploadDir,
                               @Value("${app.upload.max-size}") long maxSize,
                               @Value("${app.upload.allowed}") String allowed) {
        this.prendaRepository = prendaRepository;
        this.uploadDir = Paths.get(uploadDir);
        this.maxSize = maxSize;
        this.allowedExt = Arrays.stream(allowed.split(",")).map(String::toLowerCase).toList();
    }

    @Override
    public List<Prenda> obtenerTodas() {
        return prendaRepository.findAllWithCategoria();
    }

    @Override
    public Prenda obtenerPorId(int id) {
        return prendaRepository.findByIdWithCategoria(id).orElse(null);
    }

    @Override
    public Prenda registrarPrenda(Prenda prenda) {
        return prendaRepository.save(prenda);
    }

    @Override
    public Prenda guardarCambiosPrenda(Prenda prenda) {
        return prendaRepository.save(prenda);
    }

    @Override
    public void eliminarPrenda(int id) {
        prendaRepository.deleteById(id);
    }

    @Override
    public void guardarImagen(int idPrenda, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return;

        // validar tamaño
        if (archivo.getSize() > maxSize) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido: " + maxSize + " bytes");
        }

        // obtener extensión
        String original = archivo.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }

        if (ext.isBlank() || !allowedExt.contains(ext)) {
            throw new RuntimeException("Tipo de archivo no permitido. Extensiones permitidas: " + allowedExt);
        }

        final String extFinal = ext; // make effectively final for lambda

        prendaRepository.findById(idPrenda).ifPresent(prenda -> {
            try {
                // asegurar que la carpeta uploads/prendas existe
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String filename = "prenda-" + idPrenda + "." + extFinal;
                Path destino = uploadDir.resolve(filename);

                // escribir el archivo en disco
                Files.write(destino, archivo.getBytes());

                // guardar ruta relativa en la entidad
                prenda.setImagePath(uploadDir.toString().replace("\\", "/") + "/" + filename);

                prendaRepository.save(prenda);

            } catch (IOException e) {
                throw new RuntimeException("Error al guardar imagen de prenda", e);
            }
        });
    }

    @Override
    public List<Prenda> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return prendaRepository.findAllWithCategoria();
        }
        return prendaRepository.findByNombreContainingIgnoreCaseFetchCategoria(nombre);
    }

    @Override
    public List<Prenda> buscarPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return prendaRepository.findAllWithCategoria();
        }
        return prendaRepository.findByCategoriaNombreIgnoreCaseFetchCategoria(categoria.trim());
    }
}
