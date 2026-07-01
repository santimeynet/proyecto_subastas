package com.utn.tpi.subasta.dto.disputa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisputaRequest {

    @NotBlank
    private String motivo;

    @NotBlank
    private String descripcion;
}
