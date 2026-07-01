package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.Disputa;
import com.utn.tpi.subasta.domain.EstadoSubasta;
import com.utn.tpi.subasta.domain.Subasta;
import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.disputa.DisputaRequest;
import com.utn.tpi.subasta.dto.disputa.DisputaResponse;
import com.utn.tpi.subasta.dto.disputa.ResolverDisputaRequest;
import com.utn.tpi.subasta.exception.BusinessException;
import com.utn.tpi.subasta.repository.DisputaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisputaService {

    private final DisputaRepository disputaRepository;
    private final SubastaService subastaService;
    private final EstadoSubastaService estadoSubastaService;

    @Transactional
    public DisputaResponse abrir(Long subastaId, DisputaRequest request, Usuario usuario) {
        Subasta subasta = subastaService.buscarEntidad(subastaId);

        if (subasta.getEstado() != EstadoSubasta.ADJUDICADA) {
            throw new BusinessException("Solo se pueden abrir disputas en subastas adjudicadas");
        }
        if (disputaRepository.findBySubastaId(subastaId).isPresent()) {
            throw new BusinessException("Ya existe una disputa para esta subasta");
        }

        estadoSubastaService.abrirDisputa(subasta, usuario);

        Disputa disputa = Disputa.builder()
                .subasta(subasta)
                .iniciador(usuario)
                .motivo(request.getMotivo())
                .descripcion(request.getDescripcion())
                .build();

        return toResponse(disputaRepository.save(disputa));
    }

    @Transactional(readOnly = true)
    public List<DisputaResponse> listarPendientes() {
        return disputaRepository.findByResueltaEnIsNull().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DisputaResponse resolver(Long disputaId, ResolverDisputaRequest request, Usuario admin) {
        Disputa disputa = disputaRepository.findById(disputaId)
                .orElseThrow(() -> new BusinessException("Disputa no encontrada", HttpStatus.NOT_FOUND));

        if (disputa.getResueltaEn() != null) {
            throw new BusinessException("La disputa ya fue resuelta");
        }

        estadoSubastaService.resolverDisputa(
                disputa.getSubasta(), admin, request.getEstadoFinal(), request.getResolucion());

        disputa.setResolucion(request.getResolucion());
        disputa.setEstadoFinal(request.getEstadoFinal());
        disputa.setAdmin(admin);
        disputa.setResueltaEn(Instant.now());

        return toResponse(disputaRepository.save(disputa));
    }

    private DisputaResponse toResponse(Disputa disputa) {
        return DisputaResponse.builder()
                .id(disputa.getId())
                .subastaId(disputa.getSubasta().getId())
                .motivo(disputa.getMotivo())
                .descripcion(disputa.getDescripcion())
                .iniciadorNombre(disputa.getIniciador().getNombre())
                .resolucion(disputa.getResolucion())
                .estadoFinal(disputa.getEstadoFinal())
                .creadoEn(disputa.getCreadoEn())
                .resueltaEn(disputa.getResueltaEn())
                .build();
    }
}
