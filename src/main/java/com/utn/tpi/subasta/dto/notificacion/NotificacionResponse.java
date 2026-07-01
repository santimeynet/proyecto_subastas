package com.utn.tpi.subasta.dto.notificacion;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificacionResponse {
    private Long id;
    private String tipo;
    private String mensaje;
    private boolean leida;
    private Long subastaId;
    private Instant creadoEn;
}
