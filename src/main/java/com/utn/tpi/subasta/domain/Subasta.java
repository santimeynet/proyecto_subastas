package com.utn.tpi.subasta.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "subastas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @Column(name = "precio_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "incremento_minimo", nullable = false, precision = 19, scale = 2)
    private BigDecimal incrementoMinimo;

    @Column(name = "monto_actual", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoActual;

    @Column(name = "precio_final", precision = 19, scale = 2)
    private BigDecimal precioFinal;

    @Column(name = "inicio_utc", nullable = false)
    private Instant inicioUtc;

    @Column(name = "cierre_utc", nullable = false)
    private Instant cierreUtc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSubasta estado;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ganador_id")
    private Usuario ganador;

    @Column(name = "fecha_adjudicacion")
    private Instant fechaAdjudicacion;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private Instant creadoEn = Instant.now();

    public boolean tienePujasValidas() {
        return montoActual.compareTo(precioBase) > 0 || ganador != null;
    }
}
