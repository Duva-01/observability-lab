package com.mercadona.devops.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadona.devops.productservice.model.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
