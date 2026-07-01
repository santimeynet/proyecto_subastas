package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.dto.usuario.UsuarioResponse;
import com.utn.tpi.subasta.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    @PatchMapping("/usuarios/{id}/bloquear")
    public UsuarioResponse bloquear(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return usuarioService.bloquear(id, Boolean.TRUE.equals(body.get("bloqueado")));
    }

    @PatchMapping("/usuarios/{id}/roles")
    public UsuarioResponse actualizarRoles(@PathVariable Long id, @RequestBody Set<String> roles) {
        return usuarioService.actualizarRoles(id, roles);
    }
}
