package com.utn.tpi.subasta.repository;

import com.utn.tpi.subasta.domain.Puja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PujaRepository extends JpaRepository<Puja, Long> {

    @Query("SELECT p FROM Puja p WHERE p.subasta.id = :subastaId AND p.confirmada = true ORDER BY p.monto DESC, p.creadoEn DESC")
    List<Puja> findConfirmadasBySubastaOrderByMontoDesc(@Param("subastaId") Long subastaId);

    @Query("SELECT p FROM Puja p WHERE p.subasta.id = :subastaId AND p.confirmada = true ORDER BY p.creadoEn DESC")
    List<Puja> findConfirmadasBySubastaOrderByFechaDesc(@Param("subastaId") Long subastaId);

    default Optional<Puja> findUltimaConfirmada(Long subastaId) {
        return findConfirmadasBySubastaOrderByMontoDesc(subastaId).stream().findFirst();
    }

    boolean existsBySubastaIdAndConfirmadaTrue(Long subastaId);

    List<Puja> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    List<Puja> findBySubastaIdAndUsuarioId(Long subastaId, Long usuarioId);
}
