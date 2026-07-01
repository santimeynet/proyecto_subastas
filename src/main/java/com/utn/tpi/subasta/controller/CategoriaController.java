package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.domain.Categoria;
import com.utn.tpi.subasta.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Categoria crear(@RequestBody Map<String, String> body) {
        return categoriaService.crear(body.get("nombre"));
    }
}
