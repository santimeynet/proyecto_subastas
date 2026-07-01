package com.utn.tpi.subasta.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String nombre;
    private Set<String> roles;
}
