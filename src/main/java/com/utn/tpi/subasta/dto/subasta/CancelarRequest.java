package com.utn.tpi.subasta.dto.subasta;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelarRequest {

    @NotBlank
    private String motivo;
}
