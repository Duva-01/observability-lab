package com.mercadona.devops.orderservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadona.devops.orderservice.model.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<OrderEntity> findById(Long id);
}
