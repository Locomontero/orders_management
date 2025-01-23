package com.management.service;

import com.management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TotalValueCalculatorService {

    private final ProductRepository productRepository;

    @Autowired
    public TotalValueCalculatorService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<Double> calculateTotalValue(Long orderId) {
        return productRepository.findByOrderId(orderId)
                .map(product -> product.getPrice())
                .reduce(0.0, Double::sum);
    }
}
