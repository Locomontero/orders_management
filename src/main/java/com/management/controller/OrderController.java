package com.management.controller;

import com.management.dto.OrderDTO;
import com.management.dto.ProductDTO;
import com.management.service.OrderService;
import com.management.service.TotalValueCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final TotalValueCalculatorService totalValueCalculatorService;

    @Autowired
    public OrderController(OrderService orderService, TotalValueCalculatorService totalValueCalculatorService) {
        this.orderService = orderService;
        this.totalValueCalculatorService = totalValueCalculatorService;
    }

    @Operation(summary = "Create a new order", description = "Creates a new order in the system")
    @PostMapping
    public Mono<ResponseEntity<OrderDTO>> createOrder(@RequestBody OrderDTO orderDTO) {
        return orderService.createOrder(orderDTO)
                .map(orderDTOResult -> ResponseEntity.status(201).body(orderDTOResult))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}/calculate")
    public Mono<ResponseEntity<OrderDTO>> calculateTotalValue(@PathVariable Long id) {
        return orderService.calculateTotalValue(id)
                .map(orderDTO -> ResponseEntity.ok(orderDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all orders", description = "Fetches all orders")
    @GetMapping
    public Flux<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Get order by ID", description = "Fetches an order by its ID")
    @GetMapping("/{id}")
    public Mono<OrderDTO> getOrderWithProducts(@PathVariable Long id) {
        return orderService.getOrderWithProducts(id);
    }

    @PostMapping("/{orderId}/products")
    public Mono<ResponseEntity<OrderDTO>> addProductToOrder(@PathVariable Long orderId, @RequestBody ProductDTO productDTO) {
        return orderService.addProductToOrder(orderId, productDTO)
                .map(orderDTO -> ResponseEntity.ok(orderDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
