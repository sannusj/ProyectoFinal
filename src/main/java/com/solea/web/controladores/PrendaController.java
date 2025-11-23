package com.solea.web.controladores;

import com.solea.web.model.Prenda;
import com.solea.web.model.Categoria;
import com.solea.web.repositorios.CategoriaRepository;
import com.solea.web.servicios.ServicioPrendas;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/prendas")
public class PrendaController {

    private final ServicioPrendas servicioPrendas;
    private final CategoriaRepository categoriaRepository;

    public PrendaController(ServicioPrendas servicioPrendas, CategoriaRepository categoriaRepository) {
        this.servicioPrendas = servicioPrendas;
        this.categoriaRepository = categoriaRepository;
    }

    private void ensureDefaultCategories() {
        if (categoriaRepository.count() == 0) {
            categoriaRepository.saveAll(List.of(
                    new Categoria("Hombre"),
                    new Categoria("Mujer"),
                    new Categoria("Accesorios")
            ));
        }
    }

    /*----------------------------------------------------
     * LISTAR PRENDAS (ADMIN)
     *----------------------------------------------------*/
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("prendas", servicioPrendas.obtenerTodas());
        return "admin/prendas";  // Vista del listado
    }

    /*----------------------------------------------------
     * FORMULARIO NUEVA PRENDA
     *----------------------------------------------------*/
    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("prenda", new Prenda());
        ensureDefaultCategories();
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "admin/prenda-form";  // Vista de formulario
    }

    /*----------------------------------------------------
     * GUARDAR NUEVA PRENDA
     *----------------------------------------------------*/
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Prenda prenda,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam("imagenFile") MultipartFile imagenFile,
            Model model
    ) {
        // Asegurar categorías disponibles
        ensureDefaultCategories();

        // Resolver categoria
        Categoria cat = null;
        if (categoriaId != null) {
            cat = categoriaRepository.findById(categoriaId).orElse(null);
        } else {
            // asignar primera categoria disponible como fallback para satisfacer constraint DB
            cat = categoriaRepository.findAll().stream().findFirst().orElse(null);
        }
        prenda.setCategoria(cat);

        try {
            // Primero se guarda la prenda sin imagen
            Prenda guardada = servicioPrendas.registrarPrenda(prenda);

            // Si hay imagen, se guarda aparte
            if (imagenFile != null && !imagenFile.isEmpty()) {
                servicioPrendas.guardarImagen(guardada.getId(), imagenFile);
            }

            return "redirect:/admin/prendas";

        } catch (RuntimeException ex) {
            // Mostrar mensaje de error en el formulario
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("prenda", prenda);
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "admin/prenda-form";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Error interno al guardar la prenda");
            model.addAttribute("prenda", prenda);
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "admin/prenda-form";
        }
    }

    /*----------------------------------------------------
     * FORMULARIO EDITAR PRENDA
     *----------------------------------------------------*/
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        Prenda prenda = servicioPrendas.obtenerPorId(id);
        if (prenda == null) return "redirect:/admin/prendas";

        ensureDefaultCategories();
        model.addAttribute("prenda", prenda);
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "admin/prenda-form";
    }

    /*----------------------------------------------------
     * GUARDAR EDICIÓN
     *----------------------------------------------------*/
    @PostMapping("/{id}/editar")
    public String editarGuardar(
            @PathVariable int id,
            @ModelAttribute Prenda prendaForm,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam("imagenFile") MultipartFile imagenFile,
            Model model
    ) {
        Prenda prendaBD = servicioPrendas.obtenerPorId(id);
        if (prendaBD == null) return "redirect:/admin/prendas";

        // Se actualizan los campos editables
        prendaBD.setNombre(prendaForm.getNombre());
        prendaBD.setPrecio(prendaForm.getPrecio());
        prendaBD.setStock(prendaForm.getStock());
        prendaBD.setAlta(prendaForm.isAlta());  // si usas "alta" para activar/desactivar
        prendaBD.setDescripcion(prendaForm.getDescripcion());
        prendaBD.setTalla(prendaForm.getTalla());

        // Resolver categoria si se envió, si no asignar primera disponible
        ensureDefaultCategories();
        Categoria cat = null;
        if (categoriaId != null) {
            cat = categoriaRepository.findById(categoriaId).orElse(null);
        } else {
            cat = categoriaRepository.findAll().stream().findFirst().orElse(null);
        }
        prendaBD.setCategoria(cat);

        try {
            servicioPrendas.guardarCambiosPrenda(prendaBD);

            // Si subieron nueva imagen
            if (imagenFile != null && !imagenFile.isEmpty()) {
                servicioPrendas.guardarImagen(id, imagenFile);
            }

            return "redirect:/admin/prendas";

        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("prenda", prendaBD);
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "admin/prenda-form";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Error interno al actualizar la prenda");
            model.addAttribute("prenda", prendaBD);
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "admin/prenda-form";
        }
    }

    /*----------------------------------------------------
     * ELIMINAR PRENDA
     *----------------------------------------------------*/
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id) {
        servicioPrendas.eliminarPrenda(id);
        return "redirect:/admin/prendas";
    }
}
