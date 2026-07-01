package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.dto.disputa.DisputaRequest;
import com.utn.tpi.subasta.dto.disputa.DisputaResponse;
import com.utn.tpi.subasta.dto.disputa.ResolverDisputaRequest;
import com.utn.tpi.subasta.service.DisputaService;
import com.utn.tpi.subasta.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DisputaController {

    private final DisputaService disputaService;

    @PostMapping("/subastas/{id}/disputas")
    public DisputaResponse abrir(@PathVariable Long id, @Valid @RequestBody DisputaRequest request) {
        return disputaService.abrir(id, request, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/admin/disputas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DisputaResponse> listarPendientes() {
        return disputaService.listarPendientes();
    }

    @PatchMapping("/admin/disputas/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    public DisputaResponse resolver(@PathVariable Long id, @Valid @RequestBody ResolverDisputaRequest request) {
        return disputaService.resolver(id, request, SecurityUtils.getCurrentUser());
    }
}
