package com.mercadona.devops.orderservice.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mercadona.devops.essentials.error.BadRequestException;
import com.mercadona.devops.essentials.error.DownstreamServiceException;
import com.mercadona.devops.essentials.error.ResourceNotFoundException;
import com.mercadona.devops.orderservice.dto.CreateOrderItemRequest;
import com.mercadona.devops.orderservice.dto.CreateOrderRequest;
import com.mercadona.devops.orderservice.dto.OrderDto;
import com.mercadona.devops.orderservice.dto.OrderItemDto;
import com.mercadona.devops.orderservice.dto.ProductSummaryDto;
import com.mercadona.devops.orderservice.dto.UpdateOrderItemRequest;
import com.mercadona.devops.orderservice.dto.UpdateOrderRequest;
import com.mercadona.devops.orderservice.dto.UserSummaryDto;
import com.mercadona.devops.orderservice.model.OrderEntity;
import com.mercadona.devops.orderservice.model.OrderItemEntity;
import com.mercadona.devops.orderservice.repository.OrderRepository;
import com.mercadona.devops.orderservice.service.OrderService;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RestClient restClient;
    private final String userServiceUrl;
    private final String productServiceUrl;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            RestClient.Builder restClientBuilder,
            @Value("${downstream.user-service-url}") String userServiceUrl,
            @Value("${downstream.product-service-url}") String productServiceUrl) {
        this.orderRepository = orderRepository;
        this.restClient = restClientBuilder.build();
        this.userServiceUrl = userServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        List<OrderDto> orders = orderRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        log.info("Retrieved {} orders", orders.size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan("order.findById")
    public OrderDto findById(@SpanAttribute("order.id") Long id) {
        OrderDto order = toDto(loadOrder(id));
        log.info("Retrieved order with id {} and {} items", id, order.getItems().size());
        return order;
    }

    @Override
    @WithSpan("order.create")
    public OrderDto create(CreateOrderRequest request) {
        String status = request.getStatus() == null || request.getStatus().isBlank()
                ? "CREATED"
                : request.getStatus();

        validateUserExists(request.getUserId());
        List<OrderItemEntity> items = mapCreateItems(request.getItems());
        BigDecimal total = calculateTotal(items);

        OrderEntity entity = new OrderEntity(request.getUserId(), status, total);
        entity.replaceItems(items);
        OrderDto createdOrder = toDto(orderRepository.save(entity));
        log.info("Created order with id {} for user {} and {} items", createdOrder.getId(), request.getUserId(), createdOrder.getItems().size());
        return createdOrder;
    }

    @Override
    @WithSpan("order.update")
    public OrderDto update(@SpanAttribute("order.id") Long id, UpdateOrderRequest request) {
        OrderEntity entity = loadOrder(id);
        validateUserExists(request.getUserId());
        List<OrderItemEntity> items = mapUpdateItems(request.getItems());
        BigDecimal total = calculateTotal(items);

        entity.setUserId(request.getUserId());
        entity.setStatus(request.getStatus());
        entity.setTotal(total);
        entity.replaceItems(items);

        OrderDto updatedOrder = toDto(orderRepository.save(entity));
        log.info("Updated order with id {} to status {}", id, updatedOrder.getStatus());
        return updatedOrder;
    }

    @Override
    public void delete(Long id) {
        OrderEntity entity = loadOrder(id);
        orderRepository.delete(entity);
        log.info("Deleted order with id {}", id);
    }

    private OrderEntity loadOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private List<OrderItemEntity> mapCreateItems(List<CreateOrderItemRequest> requests) {
        List<OrderItemEntity> items = new ArrayList<>();
        for (CreateOrderItemRequest request : requests) {
            items.add(new OrderItemEntity(request.getProductId(), request.getQuantity()));
        }
        return items;
    }

    private List<OrderItemEntity> mapUpdateItems(List<UpdateOrderItemRequest> requests) {
        List<OrderItemEntity> items = new ArrayList<>();
        for (UpdateOrderItemRequest request : requests) {
            items.add(new OrderItemEntity(request.getProductId(), request.getQuantity()));
        }
        return items;
    }

    private BigDecimal calculateTotal(List<OrderItemEntity> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemEntity item : items) {
            ProductSummaryDto product = fetchProduct(item.getProductId());
            BigDecimal lineTotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
        }
        return total;
    }

    private OrderDto toDto(OrderEntity entity) {
        UserSummaryDto user = fetchUser(entity.getUserId());
        List<OrderItemDto> items = entity.getItems().stream()
                .map(this::toOrderItemDto)
                .toList();

        return new OrderDto(
                entity.getId(),
                entity.getStatus(),
                user,
                items,
                entity.getTotal().doubleValue());
    }

    private OrderItemDto toOrderItemDto(OrderItemEntity item) {
        ProductSummaryDto product = fetchProduct(item.getProductId());
        return new OrderItemDto(product, item.getQuantity());
    }

    private void validateUserExists(Long userId) {
        fetchUser(userId);
    }

    private UserSummaryDto fetchUser(Long userId) {
        try {
            log.info("Fetching user {} from user-service", userId);
            UserSummaryDto response = restClient.get()
                    .uri(userServiceUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .body(UserSummaryDto.class);
            if (response == null) {
                log.error("User service returned empty response for user {}", userId);
                throw new DownstreamServiceException("User service returned empty response");
            }
            return response;
        } catch (RestClientException exception) {
            log.error("User service unavailable while fetching user {}", userId, exception);
            throw new DownstreamServiceException("User service unavailable");
        }
    }

    private ProductSummaryDto fetchProduct(Long productId) {
        try {
            log.info("Fetching product {} from product-service", productId);
            ProductSummaryDto response = restClient.get()
                    .uri(productServiceUrl + "/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductSummaryDto.class);
            if (response == null) {
                log.error("Product service returned empty response for product {}", productId);
                throw new DownstreamServiceException("Product service returned empty response");
            }
            return response;
        } catch (RestClientException exception) {
            log.error("Product service unavailable while fetching product {}", productId, exception);
            throw new DownstreamServiceException("Product service unavailable");
        }
    }
}
