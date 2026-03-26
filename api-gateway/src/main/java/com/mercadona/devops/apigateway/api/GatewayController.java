package com.mercadona.devops.apigateway.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadona.devops.apigateway.dto.OverviewResponse;
import com.mercadona.devops.apigateway.service.GatewayService;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/overview")
    public OverviewResponse overview() {
        return gatewayService.overview();
    }

    @GetMapping("/users")
    public ResponseEntity<Object> findUsers() {
        return gatewayService.findUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> findUser(@PathVariable("id") Long id) {
        return gatewayService.findUser(id);
    }

    @GetMapping("/products")
    public ResponseEntity<Object> findProducts() {
        return gatewayService.findProducts();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> findProduct(@PathVariable("id") Long id) {
        return gatewayService.findProduct(id);
    }

    @GetMapping("/orders")
    public ResponseEntity<Object> findOrders() {
        return gatewayService.findOrders();
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Object> findOrder(@PathVariable("id") Long id) {
        return gatewayService.findOrder(id);
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestBody Object request) {
        return gatewayService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable("id") Long id, @RequestBody Object request) {
        return gatewayService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") Long id) {
        return gatewayService.deleteUser(id);
    }

    @PostMapping("/products")
    public ResponseEntity<Object> createProduct(@RequestBody Object request) {
        return gatewayService.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable("id") Long id, @RequestBody Object request) {
        return gatewayService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("id") Long id) {
        return gatewayService.deleteProduct(id);
    }

    @PostMapping("/orders")
    public ResponseEntity<Object> createOrder(@RequestBody Object request) {
        return gatewayService.createOrder(request);
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<Object> updateOrder(@PathVariable("id") Long id, @RequestBody Object request) {
        return gatewayService.updateOrder(id, request);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Object> deleteOrder(@PathVariable("id") Long id) {
        return gatewayService.deleteOrder(id);
    }
}
