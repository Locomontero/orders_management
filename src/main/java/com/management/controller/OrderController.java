package com.management.controller;

import com.management.dto.OrderDTO;
import com.management.service.OrderService;
import com.management.service.TotalValueCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Mono<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        return orderService.createOrderWithProducts(orderDTO);
    }

    @Operation(summary = "Get all orders", description = "Fetches all orders")
    @GetMapping
    public Flux<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

}
