package com.utn.tpi.subasta.repository;

import com.utn.tpi.subasta.domain.Disputa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisputaRepository extends JpaRepository<Disputa, Long> {
    Optional<Disputa> findBySubastaId(Long subastaId);
    List<Disputa> findByResueltaEnIsNull();
}
