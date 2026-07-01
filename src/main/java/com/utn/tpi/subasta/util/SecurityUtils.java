package com.utn.tpi.subasta.util;

import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new BusinessException("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
        }
        return usuario;
    }
}
