package com.utn.tpi.subasta.dto.subasta;

import com.utn.tpi.subasta.domain.EstadoSubasta;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class SubastaResponse {
    private Long id;
    private Long productoId;
    private String productoTitulo;
    private Long categoriaId;
    private String categoriaNombre;
    private Long vendedorId;
    private String vendedorNombre;
    private BigDecimal precioBase;
    private BigDecimal incrementoMinimo;
    private BigDecimal montoActual;
    private BigDecimal precioFinal;
    private Instant inicioUtc;
    private Instant cierreUtc;
    private EstadoSubasta estado;
    private String descripcion;
    private Long ganadorId;
    private String ganadorNombre;
    private Instant fechaAdjudicacion;
    private BigDecimal montoMinimoSiguientePuja;
    private Long version;
}
