package com.solea.web.repositorios;

import com.solea.web.model.Prenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrendaRepository extends JpaRepository<Prenda, Integer> {

    List<Prenda> findByNombreContainingIgnoreCase(String nombre);

    @Query("select p from Prenda p left join fetch p.categoria")
    List<Prenda> findAllWithCategoria();

    @Query("select p from Prenda p left join fetch p.categoria where lower(p.nombre) like lower(concat('%', :nombre, '%'))")
    List<Prenda> findByNombreContainingIgnoreCaseFetchCategoria(@Param("nombre") String nombre);

    @Query("select p from Prenda p left join fetch p.categoria where p.id = :id")
    java.util.Optional<Prenda> findByIdWithCategoria(@org.springframework.data.repository.query.Param("id") Integer id);

    @Query("select p from Prenda p left join fetch p.categoria c where lower(c.nombre) = lower(:categoria)")
    List<Prenda> findByCategoriaNombreIgnoreCaseFetchCategoria(@Param("categoria") String categoria);
}
