package com.utn.tpi.subasta.dto.subasta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class SubastaRequest {

    @NotNull
    private Long productoId;

    @NotNull
    private Long categoriaId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal precioBase;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal incrementoMinimo;

    @NotNull
    private Instant inicioUtc;

    @NotNull
    private Instant cierreUtc;

    private String descripcion;
}
