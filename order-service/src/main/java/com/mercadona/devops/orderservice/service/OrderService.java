package com.mercadona.devops.orderservice.service;

import java.util.List;

import com.mercadona.devops.orderservice.dto.CreateOrderRequest;
import com.mercadona.devops.orderservice.dto.OrderDto;
import com.mercadona.devops.orderservice.dto.UpdateOrderRequest;

public interface OrderService {

    List<OrderDto> findAll();

    OrderDto findById(Long id);

    OrderDto create(CreateOrderRequest request);

    OrderDto update(Long id, UpdateOrderRequest request);

    void delete(Long id);
}
