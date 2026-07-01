package com.utn.tpi.subasta.dto.puja;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PujaResponse {
    private Long id;
    private Long subastaId;
    private BigDecimal monto;
    private Instant creadoEn;
    private String oferente;
    private boolean propia;
    private Long subastaVersion;
    private BigDecimal montoMinimoSiguientePuja;
}
