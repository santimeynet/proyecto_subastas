package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.Categoria;
import com.utn.tpi.subasta.exception.BusinessException;
import com.utn.tpi.subasta.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Categoria crear(String nombre) {
        if (categoriaRepository.findByNombre(nombre).isPresent()) {
            throw new BusinessException("La categoria ya existe");
        }
        return categoriaRepository.save(Categoria.builder().nombre(nombre).build());
    }
}
