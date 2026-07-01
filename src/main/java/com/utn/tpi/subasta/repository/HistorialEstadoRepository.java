package com.utn.tpi.subasta.repository;

import com.utn.tpi.subasta.domain.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findBySubastaIdOrderByCreadoEnAsc(Long subastaId);
}
