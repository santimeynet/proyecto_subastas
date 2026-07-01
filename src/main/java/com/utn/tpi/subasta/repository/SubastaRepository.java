package com.utn.tpi.subasta.repository;

import com.utn.tpi.subasta.domain.EstadoSubasta;
import com.utn.tpi.subasta.domain.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    // Antes existia findByIdForUpdate() con @Lock(PESSIMISTIC_WRITE).
    // Se elimino: ahora la concurrencia se controla solo con bloqueo optimista
    // (campo @Version en Subasta). findById() ya viene heredado de JpaRepository.

    List<Subasta> findByEstado(EstadoSubasta estado);

    List<Subasta> findByVendedorId(Long vendedorId);

    boolean existsByProductoId(Long productoId);

    @Query("SELECT s FROM Subasta s WHERE s.estado = :estado AND s.inicioUtc <= :ahora")
    List<Subasta> findPublicadasParaActivar(@Param("estado") EstadoSubasta estado, @Param("ahora") Instant ahora);

    @Query("SELECT s FROM Subasta s WHERE s.estado = :estado AND s.cierreUtc <= :ahora")
    List<Subasta> findActivasParaCerrar(@Param("estado") EstadoSubasta estado, @Param("ahora") Instant ahora);
}
