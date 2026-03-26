package com.mercadona.devops.apigateway.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.mercadona.devops.apigateway.dto.OverviewResponse;
import com.mercadona.devops.apigateway.service.GatewayService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GatewayServiceImpl implements GatewayService {

    private final RestClient restClient;
    private final String userServiceUrl;
    private final String orderServiceUrl;
    private final String productServiceUrl;

    public GatewayServiceImpl(
            RestClient.Builder restClientBuilder,
            @Value("${downstream.user-service-url}") String userServiceUrl,
            @Value("${downstream.order-service-url}") String orderServiceUrl,
            @Value("${downstream.product-service-url}") String productServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.userServiceUrl = userServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }

    @Override
    public OverviewResponse overview() {
        return new OverviewResponse(
                "api-gateway",
                List.of(
                        "/api/users",
                        "/api/users/{id}",
                        "/api/products",
                        "/api/products/{id}",
                        "/api/orders",
                        "/api/orders/{id}"),
                Map.of(
                        "user-service", userServiceUrl,
                        "product-service", productServiceUrl,
                        "order-service", orderServiceUrl));
    }

    @Override
    public ResponseEntity<Object> findUsers() {
        return forwardGet(userServiceUrl, "/api/users");
    }

    @Override
    public ResponseEntity<Object> findUser(Long id) {
        return forwardGet(userServiceUrl, "/api/users/{id}", id);
    }

    @Override
    public ResponseEntity<Object> createUser(Object request) {
        return forwardBody("post", userServiceUrl, "/api/users", request);
    }

    @Override
    public ResponseEntity<Object> updateUser(Long id, Object request) {
        return forwardBody("put", userServiceUrl, "/api/users/{id}", request, id);
    }

    @Override
    public ResponseEntity<Object> deleteUser(Long id) {
        return forwardDelete(userServiceUrl, "/api/users/{id}", id);
    }

    @Override
    public ResponseEntity<Object> findProducts() {
        return forwardGet(productServiceUrl, "/api/products");
    }

    @Override
    public ResponseEntity<Object> findProduct(Long id) {
        return forwardGet(productServiceUrl, "/api/products/{id}", id);
    }

    @Override
    public ResponseEntity<Object> createProduct(Object request) {
        return forwardBody("post", productServiceUrl, "/api/products", request);
    }

    @Override
    public ResponseEntity<Object> updateProduct(Long id, Object request) {
        return forwardBody("put", productServiceUrl, "/api/products/{id}", request, id);
    }

    @Override
    public ResponseEntity<Object> deleteProduct(Long id) {
        return forwardDelete(productServiceUrl, "/api/products/{id}", id);
    }

    @Override
    public ResponseEntity<Object> findOrders() {
        return forwardGet(orderServiceUrl, "/api/orders");
    }

    @Override
    public ResponseEntity<Object> findOrder(Long id) {
        return forwardGet(orderServiceUrl, "/api/orders/{id}", id);
    }

    @Override
    public ResponseEntity<Object> createOrder(Object request) {
        return forwardBody("post", orderServiceUrl, "/api/orders", request);
    }

    @Override
    public ResponseEntity<Object> updateOrder(Long id, Object request) {
        return forwardBody("put", orderServiceUrl, "/api/orders/{id}", request, id);
    }

    @Override
    public ResponseEntity<Object> deleteOrder(Long id) {
        return forwardDelete(orderServiceUrl, "/api/orders/{id}", id);
    }

    private ResponseEntity<Object> forwardGet(String baseUrl, String path, Object... uriVariables) {
        String target = baseUrl + path;
        try {
            log.info("Forwarding GET request to {}", target);
            Object body = restClient.get()
                    .uri(target, uriVariables)
                    .retrieve()
                    .body(Object.class);
            log.info("GET {} completed successfully", target);
            return ResponseEntity.ok(body);
        } catch (RestClientResponseException exception) {
            log.warn("GET {} returned downstream status {}", target, exception.getStatusCode().value());
            return buildDownstreamResponse(exception);
        } catch (RestClientException exception) {
            log.error("GET {} failed because downstream is unavailable", target, exception);
            return buildUnavailableResponse(exception);
        }
    }

    private ResponseEntity<Object> forwardDelete(String baseUrl, String path, Object... uriVariables) {
        String target = baseUrl + path;
        try {
            log.info("Forwarding DELETE request to {}", target);
            restClient.delete()
                    .uri(target, uriVariables)
                    .retrieve()
                    .toBodilessEntity();
            log.info("DELETE {} completed successfully", target);
            return ResponseEntity.noContent().build();
        } catch (RestClientResponseException exception) {
            log.warn("DELETE {} returned downstream status {}", target, exception.getStatusCode().value());
            return buildDownstreamResponse(exception);
        } catch (RestClientException exception) {
            log.error("DELETE {} failed because downstream is unavailable", target, exception);
            return buildUnavailableResponse(exception);
        }
    }

    private ResponseEntity<Object> forwardBody(String method, String baseUrl, String path, Object body, Object... uriVariables) {
        String target = baseUrl + path;
        try {
            log.info("Forwarding {} request to {}", method.toUpperCase(), target);
            ResponseEntity<Object> response = switch (method) {
                case "post" -> restClient.post()
                        .uri(target, uriVariables)
                        .body(body)
                        .retrieve()
                        .toEntity(Object.class);
                case "put" -> restClient.put()
                        .uri(target, uriVariables)
                        .body(body)
                        .retrieve()
                        .toEntity(Object.class);
                default -> throw new IllegalArgumentException("Unsupported method");
            };
            log.info("{} {} completed with status {}", method.toUpperCase(), target, response.getStatusCode().value());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (RestClientResponseException exception) {
            log.warn("{} {} returned downstream status {}", method.toUpperCase(), target, exception.getStatusCode().value());
            return buildDownstreamResponse(exception);
        } catch (RestClientException exception) {
            log.error("{} {} failed because downstream is unavailable", method.toUpperCase(), target, exception);
            return buildUnavailableResponse(exception);
        }
    }

    private ResponseEntity<Object> buildDownstreamResponse(RestClientResponseException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of(
                        "error", "downstream_response_error",
                        "status", exception.getStatusCode().value(),
                        "message", exception.getResponseBodyAsString()));
    }

    private ResponseEntity<Object> buildUnavailableResponse(RestClientException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "downstream_unavailable",
                        "message", exception.getMessage()));
    }
}
