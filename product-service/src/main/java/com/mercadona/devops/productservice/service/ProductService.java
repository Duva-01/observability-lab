package com.mercadona.devops.productservice.service;

import java.util.List;

import com.mercadona.devops.productservice.dto.CreateProductRequest;
import com.mercadona.devops.productservice.dto.ProductDto;
import com.mercadona.devops.productservice.dto.UpdateProductRequest;

public interface ProductService {

    List<ProductDto> findAll();

    ProductDto findById(Long id);

    ProductDto create(CreateProductRequest request);

    ProductDto update(Long id, UpdateProductRequest request);

    void delete(Long id);
}
