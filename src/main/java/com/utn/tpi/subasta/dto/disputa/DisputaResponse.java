package com.utn.tpi.subasta.dto.disputa;

import com.utn.tpi.subasta.domain.EstadoSubasta;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DisputaResponse {
    private Long id;
    private Long subastaId;
    private String motivo;
    private String descripcion;
    private String iniciadorNombre;
    private String resolucion;
    private EstadoSubasta estadoFinal;
    private Instant creadoEn;
    private Instant resueltaEn;
}
