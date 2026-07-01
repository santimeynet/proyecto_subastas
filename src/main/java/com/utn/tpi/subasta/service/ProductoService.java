package com.utn.tpi.subasta.service;

import com.utn.tpi.subasta.domain.Producto;
import com.utn.tpi.subasta.domain.Usuario;
import com.utn.tpi.subasta.dto.producto.ProductoRequest;
import com.utn.tpi.subasta.dto.producto.ProductoResponse;
import com.utn.tpi.subasta.exception.BusinessException;
import com.utn.tpi.subasta.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional
    public ProductoResponse crear(ProductoRequest request, Usuario vendedor) {
        Producto producto = Producto.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .imagenUrl(request.getImagenUrl())
                .vendedor(vendedor)
                .build();
        return toResponse(productoRepository.save(producto));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorVendedor(Usuario vendedor) {
        return productoRepository.findByVendedorId(vendedor.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request, Usuario vendedor) {
        Producto producto = buscar(id);
        validarPropietario(producto, vendedor);
        producto.setTitulo(request.getTitulo());
        producto.setDescripcion(request.getDescripcion());
        producto.setImagenUrl(request.getImagenUrl());
        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id, Usuario vendedor) {
        Producto producto = buscar(id);
        validarPropietario(producto, vendedor);
        productoRepository.delete(producto);
    }

    public Producto buscarEntidad(Long id) {
        return buscar(id);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado", HttpStatus.NOT_FOUND));
    }

    private void validarPropietario(Producto producto, Usuario vendedor) {
        if (!producto.getVendedor().getId().equals(vendedor.getId())) {
            throw new BusinessException("No es el propietario del producto", HttpStatus.FORBIDDEN);
        }
    }

    private ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .titulo(producto.getTitulo())
                .descripcion(producto.getDescripcion())
                .imagenUrl(producto.getImagenUrl())
                .vendedorId(producto.getVendedor().getId())
                .vendedorNombre(producto.getVendedor().getNombre())
                .build();
    }
}
