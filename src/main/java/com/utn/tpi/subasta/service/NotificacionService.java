package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.Notificacion;
import com.utn.tpi.subasta.domain.Subasta;
import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.notificacion.NotificacionResponse;
import com.utn.tpi.subasta.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public void notificarAdjudicacion(Subasta subasta) {
        if (subasta.getGanador() != null) {
            crear(subasta.getGanador(), "ADJUDICACION",
                    "Felicitaciones! Ganaste la subasta #" + subasta.getId()
                            + " por $" + subasta.getPrecioFinal(),
                    subasta);
        }
        crear(subasta.getVendedor(), "ADJUDICACION",
                "Tu subasta #" + subasta.getId() + " fue adjudicada por $"
                        + subasta.getPrecioFinal(),
                subasta);
    }

    @Transactional
    public void crear(Usuario usuario, String tipo, String mensaje, Subasta subasta) {
        notificacionRepository.save(Notificacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .mensaje(mensaje)
                .subasta(subasta)
                .build());
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Usuario usuario) {
        return notificacionRepository.findByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .stream()
                .map(n -> NotificacionResponse.builder()
                        .id(n.getId())
                        .tipo(n.getTipo())
                        .mensaje(n.getMensaje())
                        .leida(n.isLeida())
                        .subastaId(n.getSubasta() != null ? n.getSubasta().getId() : null)
                        .creadoEn(n.getCreadoEn())
                        .build())
                .toList();
    }

    @Transactional
    public void marcarLeida(Long id, Usuario usuario) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new com.utn.tpi.subasta.exception.BusinessException("Notificacion no encontrada"));
        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new com.utn.tpi.subasta.exception.BusinessException("No puede marcar esta notificacion");
        }
        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }
}
