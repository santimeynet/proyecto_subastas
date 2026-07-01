package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.dto.puja.PujaRequest;
import com.utn.tpi.subasta.dto.puja.PujaResponse;
import com.utn.tpi.subasta.service.PujaService;
import com.utn.tpi.subasta.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PujaController {

    private final PujaService pujaService;

    @PostMapping("/api/subastas/{id}/pujas")
    public PujaResponse registrar(@PathVariable Long id, @Valid @RequestBody PujaRequest request) {
        return pujaService.registrar(id, request, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/api/subastas/{id}/pujas")
    public List<PujaResponse> listarPorSubasta(@PathVariable Long id) {
        return pujaService.listarPorSubasta(id, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/api/mis-pujas")
    public List<PujaResponse> misPujas() {
        return pujaService.misPujas(SecurityUtils.getCurrentUser());
    }
}
