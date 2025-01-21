package com.management.controller;

import com.management.model.Order;
import com.management.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/{id}/calculate")
    public Order calculateTotalValue(@PathVariable Long id) {
        return orderService.calculateTotalValue(id);
    }

    @GetMapping
    public List<Order> listOrders() {
        return orderService.listOrders();
    }
}
