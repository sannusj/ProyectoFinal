package com.solea.web.config;

import com.solea.web.model.Prenda;
import com.solea.web.repositorios.CategoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;

// Importaciones añadidas
import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final CategoriaRepository categoriaRepository;

    public GlobalExceptionHandler(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logger.error("Upload size exceeded", ex);
        String uri = request.getRequestURI();

        if (uri != null && uri.startsWith("/admin/prendas")) {
            ModelAndView mav = new ModelAndView("admin/prenda-form");
            mav.addObject("error", "El archivo excede el tamaño máximo permitido.");
            mav.addObject("prenda", new Prenda());
            mav.addObject("categorias", categoriaRepository.findAll());
            return mav;
        }

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "El archivo excede el tamaño máximo permitido.");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAll(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception", ex);
        String uri = request.getRequestURI();

        if (uri != null && uri.startsWith("/admin/prendas")) {
            ModelAndView mav = new ModelAndView("admin/prenda-form");
            mav.addObject("error", "Ocurrió un error al procesar la petición: " + ex.getMessage());
            mav.addObject("prenda", new Prenda());
            mav.addObject("categorias", categoriaRepository.findAll());
            return mav;
        }

        if (uri != null && uri.startsWith("/catalogo")) {
            ModelAndView mav = new ModelAndView("home/catalogo");
            mav.addObject("error", "Ocurrió un error al cargar el catálogo. Intenta nuevamente más tarde.");
            mav.addObject("productos", Collections.emptyList());
            return mav;
        }

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "Se produjo un error en el servidor. Intenta nuevamente más tarde.");
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("message", ex.getMessage());
        mav.addObject("status", 403);
        return mav;
    }
}
