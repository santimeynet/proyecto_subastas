package com.utn.tpi.subasta.dto.usuario;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String email;
    private String nombre;
    private boolean bloqueado;
    private Set<String> roles;
}
