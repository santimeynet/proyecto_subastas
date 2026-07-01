package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.dto.notificacion.NotificacionResponse;
import com.utn.tpi.subasta.service.NotificacionService;
import com.utn.tpi.subasta.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public List<NotificacionResponse> listar() {
        return notificacionService.listarPorUsuario(SecurityUtils.getCurrentUser());
    }

    @PatchMapping("/{id}/leer")
    public void marcarLeida(@PathVariable Long id) {
        notificacionService.marcarLeida(id, SecurityUtils.getCurrentUser());
    }
}
