package com.utn.tpi.subasta.dto.producto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductoRequest {

    @NotBlank
    private String titulo;

    private String descripcion;
    private String imagenUrl;
}
