package com.utn.tpi.subasta.dto.disputa;

import com.utn.tpi.subasta.domain.EstadoSubasta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolverDisputaRequest {

    @NotBlank
    private String resolucion;

    @NotNull
    private EstadoSubasta estadoFinal;
}
