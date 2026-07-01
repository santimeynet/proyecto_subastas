package com.utn.tpi.subasta.dto.historial;

import com.utn.tpi.subasta.domain.EstadoSubasta;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class HistorialEstadoResponse {
    private Long id;
    private EstadoSubasta estadoAnterior;
    private EstadoSubasta estadoNuevo;
    private String usuarioNombre;
    private String motivo;
    private Instant creadoEn;
}
