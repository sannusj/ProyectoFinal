package com.solea.web.controladores;

import com.solea.web.model.Prenda;
import com.solea.web.model.Categoria;
import com.solea.web.repositorios.CategoriaRepository;
import com.solea.web.servicios.ServicioPrendas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/prendas")
@Tag(
    name = "Prendas", 
    description = "API de gestión de prendas y productos con acceso restringido a administradores. " +
                  "Permite crear, editar, eliminar y listar productos del catálogo. " +
                  "Incluye gestión de imágenes, categorías, stock y precios. " +
                  "Requiere autenticación con rol ADMIN."
)
@SecurityRequirement(name = "session-auth")
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
    @Operation(
            summary = "Listar todas las prendas del catálogo",
            description = "Muestra el listado completo de todas las prendas/productos del sistema incluyendo: " +
                         "ID, nombre, precio, stock, categoría, estado (alta/baja) e imagen. " +
                         "Permite acciones de edición y eliminación. Acceso exclusivo para administradores."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Lista de prendas obtenida exitosamente con toda su información"
    )
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("prendas", servicioPrendas.obtenerTodas());
        return "admin/prendas";  // Vista del listado
    }

    /*----------------------------------------------------
     * FORMULARIO NUEVA PRENDA
     *----------------------------------------------------*/
    @Operation(
            summary = "Formulario de nueva prenda",
            description = "Muestra el formulario para crear una nueva prenda/producto en el catálogo. " +
                         "El formulario incluye campos para: nombre, precio, stock, descripción, talla, " +
                         "categoría y carga de imagen. Asegura que existan categorías por defecto " +
                         "(Hombre, Mujer, Accesorios) antes de mostrar el formulario."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Formulario de nueva prenda cargado exitosamente con lista de categorías disponibles"
    )
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
    @Operation(
            summary = "Guardar nueva prenda en el catálogo",
            description = "Procesa y guarda una nueva prenda/producto en el sistema. " +
                         "Valida todos los datos requeridos, asigna la categoría especificada (o una por defecto), " +
                         "procesa y almacena la imagen del producto, y actualiza el inventario. " +
                         "Si hay errores de validación o almacenamiento, retorna al formulario con mensajes descriptivos. " +
                         "La imagen se guarda primero en el sistema de archivos y se referencia en la base de datos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Prenda guardada exitosamente con imagen - redirige al listado de prendas"
            ),
            @ApiResponse(
                    responseCode = "200", 
                    description = "Error en validación o guardado - retorna al formulario con mensaje de error"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Datos inválidos, imagen corrupta o error de categoría"
            )
    })
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Prenda prenda,
            @Parameter(description = "ID de la categoría del producto", example = "1", required = false)
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @Parameter(description = "Archivo de imagen del producto (PNG, JPG, JPEG)", required = true)
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
    @Operation(
            summary = "Formulario de edición de prenda",
            description = "Muestra el formulario de edición para una prenda existente, pre-cargado con sus datos actuales: " +
                         "nombre, precio, stock, categoría, descripción, talla e imagen. " +
                         "Si la prenda no existe, redirige al listado. " +
                         "Permite modificar todos los campos incluyendo cambiar la imagen."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de edición cargado con datos de la prenda"),
            @ApiResponse(responseCode = "302", description = "Prenda no encontrada - redirige al listado")
    })
    @GetMapping("/{id}/editar")
    public String editar(
            @Parameter(description = "ID de la prenda a editar", required = true, example = "1")
            @PathVariable int id, Model model) {
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
    @Operation(
            summary = "Guardar cambios de prenda editada",
            description = "Actualiza una prenda existente con los nuevos datos proporcionados. " +
                         "Permite modificar nombre, precio, stock, estado (alta/baja), descripción, talla y categoría. " +
                         "Si se proporciona una nueva imagen, reemplaza la anterior. " +
                         "Valida todos los campos y mantiene integridad de datos. " +
                         "Si hay errores, retorna al formulario con los datos y mensaje de error."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Prenda actualizada exitosamente - redirige al listado"
            ),
            @ApiResponse(
                    responseCode = "200", 
                    description = "Error al actualizar - retorna al formulario con mensaje"
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Prenda no encontrada"
            )
    })
    @PostMapping("/{id}/editar")
    public String editarGuardar(
            @Parameter(description = "ID de la prenda a actualizar", required = true, example = "1")
            @PathVariable int id,
            @ModelAttribute Prenda prendaForm,
            @Parameter(description = "ID de la nueva categoría", example = "2", required = false)
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @Parameter(description = "Nueva imagen (opcional - solo si se desea cambiar)", required = false)
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
    @Operation(
            summary = "Eliminar prenda del catálogo",
            description = "Elimina permanentemente una prenda del sistema. " +
                         "Esta acción también elimina la imagen asociada y todos los registros relacionados. " +
                         "Usar con precaución: la eliminación es irreversible. " +
                         "Considerar marcar como 'baja' en lugar de eliminar para mantener historial."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Prenda eliminada exitosamente - redirige al listado"
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Prenda no encontrada"
            )
    })
    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @Parameter(description = "ID de la prenda a eliminar", required = true, example = "1")
            @PathVariable int id) {
        servicioPrendas.eliminarPrenda(id);
        return "redirect:/admin/prendas";
    }
}