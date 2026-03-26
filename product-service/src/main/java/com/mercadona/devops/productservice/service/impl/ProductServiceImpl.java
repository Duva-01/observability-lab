package com.mercadona.devops.productservice.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadona.devops.essentials.error.ResourceNotFoundException;
import com.mercadona.devops.productservice.dto.CreateProductRequest;
import com.mercadona.devops.productservice.dto.ProductDto;
import com.mercadona.devops.productservice.dto.UpdateProductRequest;
import com.mercadona.devops.productservice.model.ProductEntity;
import com.mercadona.devops.productservice.repository.ProductRepository;
import com.mercadona.devops.productservice.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        List<ProductDto> products = productRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        log.info("Retrieved {} products", products.size());
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        ProductDto product = toDto(loadProduct(id));
        log.info("Retrieved product with id {}", id);
        return product;
    }

    @Override
    public ProductDto create(CreateProductRequest request) {
        ProductEntity entity = new ProductEntity(
                request.getName(),
                request.getCategory(),
                BigDecimal.valueOf(request.getPrice()));
        ProductDto createdProduct = toDto(productRepository.save(entity));
        log.info("Created product with id {} and category {}", createdProduct.getId(), createdProduct.getCategory());
        return createdProduct;
    }

    @Override
    public ProductDto update(Long id, UpdateProductRequest request) {
        ProductEntity entity = loadProduct(id);
        entity.setName(request.getName());
        entity.setCategory(request.getCategory());
        entity.setPrice(BigDecimal.valueOf(request.getPrice()));
        ProductDto updatedProduct = toDto(productRepository.save(entity));
        log.info("Updated product with id {}", id);
        return updatedProduct;
    }

    @Override
    public void delete(Long id) {
        ProductEntity entity = loadProduct(id);
        productRepository.delete(entity);
        log.info("Deleted product with id {}", id);
    }

    private ProductEntity loadProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private ProductDto toDto(ProductEntity entity) {
        return new ProductDto(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getPrice().doubleValue());
    }
}
