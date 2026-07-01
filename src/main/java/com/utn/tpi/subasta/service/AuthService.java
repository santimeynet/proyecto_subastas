package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.Rol;
import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.auth.AuthResponse;
import com.utn.tpi.subasta.dto.auth.LoginRequest;
import com.utn.tpi.subasta.dto.auth.RegisterRequest;
import com.utn.tpi.subasta.exception.BusinessException;
import com.utn.tpi.subasta.repository.RolRepository;
import com.utn.tpi.subasta.repository.UsuarioRepository;
import com.utn.tpi.subasta.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya esta registrado");
        }

        Set<Rol> roles = new HashSet<>();
        Rol userRol = rolRepository.findByNombre("USER").orElseThrow();
        roles.add(userRol);

        if (request.getRoles() != null) {
            for (String rolNombre : request.getRoles()) {
                if ("SELLER".equals(rolNombre)) {
                    rolRepository.findByNombre("SELLER").ifPresent(roles::add);
                }
            }
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .roles(roles)
                .build();

        usuarioRepository.save(usuario);
        return buildAuthResponse(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        return buildAuthResponse(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse me(Usuario usuario) {
        return buildAuthResponse(usuario);
    }

    private AuthResponse buildAuthResponse(Usuario usuario) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(usuario))
                .userId(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .roles(usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet()))
                .build();
    }
}
