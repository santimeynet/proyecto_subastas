package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.*;
import com.utn.tpi.subasta.exception.BusinessException;
import com.utn.tpi.subasta.repository.HistorialEstadoRepository;
import com.utn.tpi.subasta.repository.PujaRepository;
import com.utn.tpi.subasta.repository.SubastaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EstadoSubastaService {

    private final SubastaRepository subastaRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final PujaRepository pujaRepository;
    private final NotificacionService notificacionService;

    @Transactional
    public Subasta publicar(Subasta subasta, Usuario usuario) {
        validarTransicion(subasta.getEstado(), EstadoSubasta.PUBLICADA);
        if (!subasta.getVendedor().getId().equals(usuario.getId())) {
            throw new BusinessException("Solo el vendedor puede publicar la subasta", HttpStatus.FORBIDDEN);
        }
        return cambiarEstado(subasta, EstadoSubasta.PUBLICADA, usuario, "Subasta publicada");
    }

    @Transactional
    public Subasta activar(Subasta subasta) {
        validarTransicion(subasta.getEstado(), EstadoSubasta.ACTIVA);
        return cambiarEstado(subasta, EstadoSubasta.ACTIVA, null, "Inicio automatico de subasta");
    }

    @Transactional
    public Subasta finalizar(Subasta subasta, Usuario usuario, String motivo) {
        validarTransicion(subasta.getEstado(), EstadoSubasta.FINALIZADA);
        return cambiarEstado(subasta, EstadoSubasta.FINALIZADA, usuario, motivo);
    }

    @Transactional
    public Subasta adjudicar(Subasta subasta, String motivo) {
        validarTransicion(subasta.getEstado(), EstadoSubasta.ADJUDICADA);
        subasta.setPrecioFinal(subasta.getMontoActual());
        subasta.setFechaAdjudicacion(Instant.now());
        Subasta guardada = cambiarEstado(subasta, EstadoSubasta.ADJUDICADA, null, motivo);

        if (guardada.getGanador() != null) {
            notificacionService.notificarAdjudicacion(guardada);
        }
        return guardada;
    }

    @Transactional
    public Subasta cancelar(Subasta subasta, Usuario usuario, String motivo, boolean esAdmin) {
        validarTransicion(subasta.getEstado(), EstadoSubasta.CANCELADA);
        boolean tienePujas = pujaRepository.existsBySubastaIdAndConfirmadaTrue(subasta.getId());

        if (tienePujas && !esAdmin) {
            throw new BusinessException("Solo un administrador puede cancelar subastas con pujas", HttpStatus.FORBIDDEN);
        }
        if (!tienePujas && !esAdmin && !subasta.getVendedor().getId().equals(usuario.getId())) {
            throw new BusinessException("Solo el vendedor puede cancelar subastas sin pujas", HttpStatus.FORBIDDEN);
        }

        return cambiarEstado(subasta, EstadoSubasta.CANCELADA, usuario, motivo);
    }

    @Transactional
    public Subasta abrirDisputa(Subasta subasta, Usuario usuario) {
        validarTransicion(subasta.getEstado(), EstadoSubasta.EN_DISPUTA);
        if (!subasta.getEstado().equals(EstadoSubasta.ADJUDICADA)) {
            throw new BusinessException("Solo subastas adjudicadas pueden entrar en disputa");
        }
        boolean esVendedor = subasta.getVendedor().getId().equals(usuario.getId());
        boolean esGanador = subasta.getGanador() != null && subasta.getGanador().getId().equals(usuario.getId());
        if (!esVendedor && !esGanador) {
            throw new BusinessException("Solo el vendedor o el ganador pueden abrir una disputa", HttpStatus.FORBIDDEN);
        }
        return cambiarEstado(subasta, EstadoSubasta.EN_DISPUTA, usuario, "Disputa abierta");
    }

    @Transactional
    public Subasta resolverDisputa(Subasta subasta, Usuario admin, EstadoSubasta estadoFinal, String resolucion) {
        if (!subasta.getEstado().equals(EstadoSubasta.EN_DISPUTA)) {
            throw new BusinessException("La subasta no esta en disputa");
        }
        if (estadoFinal != EstadoSubasta.ADJUDICADA && estadoFinal != EstadoSubasta.CANCELADA) {
            throw new BusinessException("El administrador solo puede resolver a ADJUDICADA o CANCELADA");
        }
        return cambiarEstado(subasta, estadoFinal, admin, resolucion);
    }

    private Subasta cambiarEstado(Subasta subasta, EstadoSubasta nuevoEstado, Usuario usuario, String motivo) {
        EstadoSubasta anterior = subasta.getEstado();
        subasta.setEstado(nuevoEstado);
        Subasta guardada = subastaRepository.save(subasta);

        historialEstadoRepository.save(HistorialEstado.builder()
                .subasta(guardada)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevoEstado)
                .usuario(usuario)
                .motivo(motivo)
                .build());

        return guardada;
    }

    private void validarTransicion(EstadoSubasta actual, EstadoSubasta destino) {
        boolean valida = switch (actual) {
            case BORRADOR -> destino == EstadoSubasta.PUBLICADA;
            case PUBLICADA -> destino == EstadoSubasta.ACTIVA || destino == EstadoSubasta.CANCELADA;
            case ACTIVA -> destino == EstadoSubasta.FINALIZADA
                    || destino == EstadoSubasta.ADJUDICADA
                    || destino == EstadoSubasta.CANCELADA;
            case ADJUDICADA -> destino == EstadoSubasta.EN_DISPUTA;
            case EN_DISPUTA -> destino == EstadoSubasta.ADJUDICADA
                    || destino == EstadoSubasta.FINALIZADA
                    || destino == EstadoSubasta.CANCELADA;
            default -> false;
        };
        if (!valida) {
            throw new BusinessException(
                    "Transicion de estado invalida: " + actual + " -> " + destino);
        }
    }
}
