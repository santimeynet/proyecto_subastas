package com.utn.tpi.subasta.config;

import com.utn.tpi.subasta.domain.Rol;
import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.repository.RolRepository;
import com.utn.tpi.subasta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail("admin@utn.edu.ar").isEmpty()) {
            Rol admin = rolRepository.findByNombre("ADMIN").orElseThrow();
            Rol user = rolRepository.findByNombre("USER").orElseThrow();

            Usuario usuario = Usuario.builder()
                    .email("admin@utn.edu.ar")
                    .passwordHash(passwordEncoder.encode("Admin123!"))
                    .nombre("Administrador")
                    .roles(Set.of(admin, user))
                    .build();
            usuarioRepository.save(usuario);
        }
    }
}
