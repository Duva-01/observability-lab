package com.mercadona.devops.apigateway.service;

import org.springframework.http.ResponseEntity;

import com.mercadona.devops.apigateway.dto.OverviewResponse;

public interface GatewayService {

    OverviewResponse overview();

    ResponseEntity<Object> findUsers();

    ResponseEntity<Object> findUser(Long id);

    ResponseEntity<Object> createUser(Object request);

    ResponseEntity<Object> updateUser(Long id, Object request);

    ResponseEntity<Object> deleteUser(Long id);

    ResponseEntity<Object> findProducts();

    ResponseEntity<Object> findProduct(Long id);

    ResponseEntity<Object> createProduct(Object request);

    ResponseEntity<Object> updateProduct(Long id, Object request);

    ResponseEntity<Object> deleteProduct(Long id);

    ResponseEntity<Object> findOrders();

    ResponseEntity<Object> findOrder(Long id);

    ResponseEntity<Object> createOrder(Object request);

    ResponseEntity<Object> updateOrder(Long id, Object request);

    ResponseEntity<Object> deleteOrder(Long id);
}
