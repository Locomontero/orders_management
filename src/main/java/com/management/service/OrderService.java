package com.management.service;

import com.management.model.Order;
import com.management.model.Product;
import com.management.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order calculateTotalValue(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    double totalValue = order.getProducts().stream()
                            .mapToDouble(Product::getPrice)
                            .sum();
                    order.setTotalValue(totalValue);
                    return orderRepository.save(order);
                }).orElse(null);
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }
}
