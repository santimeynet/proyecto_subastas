package com.utn.tpi.subasta.controller;

import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.auth.AuthResponse;
import com.utn.tpi.subasta.dto.auth.LoginRequest;
import com.utn.tpi.subasta.dto.auth.RegisterRequest;
import com.utn.tpi.subasta.service.AuthService;
import com.utn.tpi.subasta.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me() {
        Usuario usuario = SecurityUtils.getCurrentUser();
        return authService.me(usuario);
    }
}
