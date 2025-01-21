package com.management.service;

import com.management.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Service
public class ProductService {

    private final WebClient.Builder webClientBuilder;
    private final OrderService orderService;

    @Autowired
    public ProductService(WebClient.Builder webClientBuilder, OrderService orderService) {
        this.webClientBuilder = webClientBuilder;
        this.orderService = orderService;
    }

    private static final String PRODUCT_A_URL = "https://api.produtoA.com/v1/products";

    public Flux<Product> getExternalProduct() {
        return createProducts(1L);
    }

    public Mono<Product> getExternalProduct(Long id) {
        return createProducts(1L)
                .filter(product -> product.getId().equals(id))
                .next();
    }

    private Flux<Product> createProducts(Long orderId) {
        return Optional.ofNullable(orderService.getOrderById(orderId))
                .map(order -> Flux.just(
                        new Product(1L, "Product A", 100.0, order),
                        new Product(2L, "Product B", 50.5, order),
                        new Product(3L, "Product C", 75.0, order)
                ))
                .orElse(Flux.empty());
    }
}
