package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.EstadoSubasta;
import com.utn.tpi.subasta.domain.Puja;
import com.utn.tpi.subasta.domain.Subasta;
import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.puja.PujaRequest;
import com.utn.tpi.subasta.dto.puja.PujaResponse;
import com.utn.tpi.subasta.exception.BusinessException;
import com.utn.tpi.subasta.repository.PujaRepository;
import com.utn.tpi.subasta.repository.SubastaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PujaService {

    private final SubastaRepository subastaRepository;
    private final PujaRepository pujaRepository;

    @Transactional
    public PujaResponse registrar(Long subastaId, PujaRequest request, Usuario usuario) {
        if (usuario.isBloqueado()) {
            throw new BusinessException("Usuario bloqueado, no puede pujar");
        }

        // Antes: subastaRepository.findByIdForUpdate(subastaId) con PESSIMISTIC_WRITE.
        // Ahora la concurrencia se resuelve solo con bloqueo optimista: se lee
        // normal, y si dos pujas chocan, el save() de mas abajo falla por @Version
        // desactualizada (lo captura GlobalExceptionHandler como 409 CONFLICT).
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new BusinessException("Subasta no encontrada", HttpStatus.NOT_FOUND));

        validarSubastaParaPuja(subasta, usuario);

        if (!request.getVersion().equals(subasta.getVersion())) {
            BigDecimal minimoActual = calcularMontoMinimo(subasta);
            throw new BusinessException(
                    "El precio actual cambio. El monto minimo es $" + minimoActual
                            + ". Actualice la pantalla e intente nuevamente.",
                    HttpStatus.CONFLICT);
        }

        Optional<Puja> ultimaPuja = pujaRepository.findUltimaConfirmada(subastaId);
        BigDecimal montoMinimo = ultimaPuja
                .map(p -> p.getMonto().add(subasta.getIncrementoMinimo()))
                .orElse(subasta.getPrecioBase());

        if (request.getMonto().compareTo(montoMinimo) < 0) {
            throw new BusinessException(
                    "El monto debe ser al menos $" + montoMinimo
                            + ". Hay nuevas pujas: actualice e intente nuevamente.",
                    HttpStatus.CONFLICT);
        }

        Puja puja = Puja.builder()
                .subasta(subasta)
                .usuario(usuario)
                .monto(request.getMonto())
                .confirmada(true)
                .build();
        pujaRepository.save(puja);

        subasta.setMontoActual(request.getMonto());
        subasta.setGanador(usuario);
        subastaRepository.save(subasta);

        BigDecimal siguienteMinimo = request.getMonto().add(subasta.getIncrementoMinimo());
        return toResponse(puja, usuario, true, subasta.getVersion(), siguienteMinimo);
    }

    @Transactional(readOnly = true)
    public List<PujaResponse> listarPorSubasta(Long subastaId, Usuario usuario) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new BusinessException("Subasta no encontrada", HttpStatus.NOT_FOUND));

        boolean esAdmin = usuario.tieneRol("ADMIN");
        boolean esVendedor = subasta.getVendedor().getId().equals(usuario.getId());
        boolean subastaCerrada = subasta.getEstado() == EstadoSubasta.FINALIZADA
                || subasta.getEstado() == EstadoSubasta.ADJUDICADA
                || subasta.getEstado() == EstadoSubasta.EN_DISPUTA;

        List<Puja> pujas = pujaRepository.findConfirmadasBySubastaOrderByFechaDesc(subastaId);

        if (esAdmin || (esVendedor && subastaCerrada)) {
            return pujas.stream()
                    .map(p -> toResponse(p, usuario, true, subasta.getVersion(), null))
                    .toList();
        }

        if (subasta.getEstado() == EstadoSubasta.ACTIVA) {
            return pujas.stream()
                    .filter(p -> p.getUsuario().getId().equals(usuario.getId()))
                    .map(p -> toResponse(p, usuario, true, subasta.getVersion(), null))
                    .toList();
        }

        return pujas.stream()
                .filter(p -> p.getUsuario().getId().equals(usuario.getId()))
                .map(p -> toResponse(p, usuario, true, subasta.getVersion(), null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PujaResponse> misPujas(Usuario usuario) {
        return pujaRepository.findByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .stream()
                .map(p -> toResponse(p, usuario, true, p.getSubasta().getVersion(), null))
                .toList();
    }

    private BigDecimal calcularMontoMinimo(Subasta subasta) {
        return pujaRepository.findUltimaConfirmada(subasta.getId())
                .map(p -> p.getMonto().add(subasta.getIncrementoMinimo()))
                .orElse(subasta.getPrecioBase());
    }

    private void validarSubastaParaPuja(Subasta subasta, Usuario usuario) {
        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new BusinessException("La subasta no esta activa");
        }
        if (!Instant.now().isBefore(subasta.getCierreUtc())) {
            throw new BusinessException("La subasta ya finalizo");
        }
        if (subasta.getVendedor().getId().equals(usuario.getId())) {
            throw new BusinessException("El vendedor no puede pujar en su propia subasta");
        }
    }

    private PujaResponse toResponse(Puja puja, Usuario viewer, boolean mostrarIdentidad,
                                    Long subastaVersion, BigDecimal montoMinimoSiguiente) {
        String oferente;
        if (mostrarIdentidad || viewer.tieneRol("ADMIN")) {
            oferente = puja.getUsuario().getNombre();
        } else {
            oferente = "Oferente anonimo";
        }

        return PujaResponse.builder()
                .id(puja.getId())
                .subastaId(puja.getSubasta().getId())
                .monto(puja.getMonto())
                .creadoEn(puja.getCreadoEn())
                .oferente(oferente)
                .propia(puja.getUsuario().getId().equals(viewer.getId()))
                .subastaVersion(subastaVersion)
                .montoMinimoSiguientePuja(montoMinimoSiguiente)
                .build();
    }
}
