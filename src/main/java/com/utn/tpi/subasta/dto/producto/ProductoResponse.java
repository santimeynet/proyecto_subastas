package com.utn.tpi.subasta.dto.producto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private Long vendedorId;
    private String vendedorNombre;
}
