package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.producto.ProductoRequest;
import com.utn.tpi.subasta.dto.producto.ProductoResponse;
import com.utn.tpi.subasta.service.ProductoService;
import com.utn.tpi.subasta.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        Usuario vendedor = SecurityUtils.getCurrentUser();
        return productoService.crear(request, vendedor);
    }

    @GetMapping("/mis-productos")
    @PreAuthorize("hasRole('SELLER')")
    public List<ProductoResponse> misProductos() {
        return productoService.listarPorVendedor(SecurityUtils.getCurrentUser());
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request, SecurityUtils.getCurrentUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id, SecurityUtils.getCurrentUser());
    }
}
