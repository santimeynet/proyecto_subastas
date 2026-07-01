package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.domain.EstadoSubasta;
import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.historial.HistorialEstadoResponse;
import com.utn.tpi.subasta.dto.subasta.CancelarRequest;
import com.utn.tpi.subasta.dto.subasta.SubastaRequest;
import com.utn.tpi.subasta.dto.subasta.SubastaResponse;
import com.utn.tpi.subasta.service.SubastaService;
import com.utn.tpi.subasta.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subastas")
@RequiredArgsConstructor
public class SubastaController {

    private final SubastaService subastaService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public SubastaResponse crear(@Valid @RequestBody SubastaRequest request) {
        return subastaService.crear(request, SecurityUtils.getCurrentUser());
    }

    @GetMapping
    public List<SubastaResponse> listar(@RequestParam(required = false) EstadoSubasta estado) {
        return subastaService.listar(estado);
    }

    @GetMapping("/{id}")
    public SubastaResponse obtener(@PathVariable Long id) {
        return subastaService.obtener(id);
    }

    @PostMapping("/{id}/publicar")
    @PreAuthorize("hasRole('SELLER')")
    public SubastaResponse publicar(@PathVariable Long id) {
        return subastaService.publicar(id, SecurityUtils.getCurrentUser());
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public SubastaResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarRequest request) {
        Usuario usuario = SecurityUtils.getCurrentUser();
        return subastaService.cancelar(id, usuario, request.getMotivo());
    }

    @GetMapping("/{id}/historial")
    public List<HistorialEstadoResponse> historial(@PathVariable Long id) {
        return subastaService.historial(id);
    }
}
