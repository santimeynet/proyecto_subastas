package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.*;
import com.utn.tpi.subasta.dto.historial.HistorialEstadoResponse;
import com.utn.tpi.subasta.dto.subasta.SubastaRequest;
import com.utn.tpi.subasta.dto.subasta.SubastaResponse;
import com.utn.tpi.subasta.exception.BusinessException;
import com.utn.tpi.subasta.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubastaService {

    private final SubastaRepository subastaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PujaRepository pujaRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final EstadoSubastaService estadoSubastaService;

    @Transactional
    public SubastaResponse crear(SubastaRequest request, Usuario vendedor) {
        validarFechas(request.getInicioUtc(), request.getCierreUtc());

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));
        if (!producto.getVendedor().getId().equals(vendedor.getId())) {
            throw new BusinessException("El producto no pertenece al vendedor", HttpStatus.FORBIDDEN);
        }
        if (subastaRepository.existsByProductoId(producto.getId())) {
            throw new BusinessException("El producto ya tiene una subasta asociada");
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new BusinessException("Categoria no encontrada"));

        Subasta subasta = Subasta.builder()
                .producto(producto)
                .categoria(categoria)
                .vendedor(vendedor)
                .precioBase(request.getPrecioBase())
                .incrementoMinimo(request.getIncrementoMinimo())
                .montoActual(request.getPrecioBase())
                .inicioUtc(request.getInicioUtc())
                .cierreUtc(request.getCierreUtc())
                .descripcion(request.getDescripcion())
                .estado(EstadoSubasta.BORRADOR)
                .build();

        Subasta guardada = subastaRepository.save(subasta);
        historialEstadoRepository.save(HistorialEstado.builder()
                .subasta(guardada)
                .estadoNuevo(EstadoSubasta.BORRADOR)
                .usuario(vendedor)
                .motivo("Subasta creada en borrador")
                .build());

        return toResponse(guardada);
    }

    @Transactional
    public SubastaResponse publicar(Long id, Usuario vendedor) {
        Subasta subasta = buscar(id);
        estadoSubastaService.publicar(subasta, vendedor);
        return toResponse(subasta);
    }

    @Transactional
    public SubastaResponse cancelar(Long id, Usuario usuario, String motivo) {
        Subasta subasta = buscar(id);
        boolean esAdmin = usuario.tieneRol("ADMIN");
        estadoSubastaService.cancelar(subasta, usuario, motivo, esAdmin);
        return toResponse(subasta);
    }

    @Transactional
    public List<SubastaResponse> listar(EstadoSubasta estado) {
        procesarTransicionesAutomaticas();
        List<Subasta> subastas = estado != null
                ? subastaRepository.findByEstado(estado)
                : subastaRepository.findAll();
        return subastas.stream().map(this::toResponse).toList();
    }

    @Transactional
    public SubastaResponse obtener(Long id) {
        procesarTransicionesAutomaticas();
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> historial(Long id) {
        buscar(id);
        return historialEstadoRepository.findBySubastaIdOrderByCreadoEnAsc(id).stream()
                .map(h -> HistorialEstadoResponse.builder()
                        .id(h.getId())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .usuarioNombre(h.getUsuario() != null ? h.getUsuario().getNombre() : "Sistema")
                        .motivo(h.getMotivo())
                        .creadoEn(h.getCreadoEn())
                        .build())
                .toList();
    }

    @Transactional
    public void procesarTransicionesAutomaticas() {
        Instant ahora = Instant.now();

        subastaRepository.findPublicadasParaActivar(EstadoSubasta.PUBLICADA, ahora)
                .forEach(estadoSubastaService::activar);

        subastaRepository.findActivasParaCerrar(EstadoSubasta.ACTIVA, ahora)
                .forEach(subasta -> {
                    boolean tienePujas = pujaRepository.existsBySubastaIdAndConfirmadaTrue(subasta.getId());
                    if (tienePujas) {
                        estadoSubastaService.adjudicar(subasta, "Cierre automatico con pujas");
                    } else {
                        estadoSubastaService.finalizar(subasta, null, "Cierre automatico sin pujas");
                    }
                });
    }

    public Subasta buscarEntidad(Long id) {
        return buscar(id);
    }

    private Subasta buscar(Long id) {
        return subastaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Subasta no encontrada", HttpStatus.NOT_FOUND));
    }

    private void validarFechas(Instant inicio, Instant cierre) {
        if (!cierre.isAfter(inicio)) {
            throw new BusinessException("La fecha de cierre debe ser posterior a la de inicio");
        }
        if (inicio.isBefore(Instant.now())) {
            throw new BusinessException("La fecha de inicio es anterior a la hora actual");
        }
    }

    private SubastaResponse toResponse(Subasta subasta) {
        boolean tienePujas = pujaRepository.existsBySubastaIdAndConfirmadaTrue(subasta.getId());
        BigDecimal minimo = tienePujas
                ? subasta.getMontoActual().add(subasta.getIncrementoMinimo())
                : subasta.getPrecioBase();

        return SubastaResponse.builder()
                .id(subasta.getId())
                .productoId(subasta.getProducto().getId())
                .productoTitulo(subasta.getProducto().getTitulo())
                .categoriaId(subasta.getCategoria().getId())
                .categoriaNombre(subasta.getCategoria().getNombre())
                .vendedorId(subasta.getVendedor().getId())
                .vendedorNombre(subasta.getVendedor().getNombre())
                .precioBase(subasta.getPrecioBase())
                .incrementoMinimo(subasta.getIncrementoMinimo())
                .montoActual(subasta.getMontoActual())
                .precioFinal(subasta.getPrecioFinal())
                .inicioUtc(subasta.getInicioUtc())
                .cierreUtc(subasta.getCierreUtc())
                .estado(subasta.getEstado())
                .descripcion(subasta.getDescripcion())
                .ganadorId(subasta.getGanador() != null ? subasta.getGanador().getId() : null)
                .ganadorNombre(subasta.getGanador() != null ? subasta.getGanador().getNombre() : null)
                .fechaAdjudicacion(subasta.getFechaAdjudicacion())
                .montoMinimoSiguientePuja(minimo)
                .version(subasta.getVersion())
                .build();
    }
}
