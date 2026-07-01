package com.utn.tpi.subasta.repository;

import com.utn.tpi.subasta.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByVendedorId(Long vendedorId);
}
