package com.management.service;

import com.management.model.Product;
import com.management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
public class ProductService {

    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ProductService(OrderService orderService, ProductRepository productRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Flux<Product> getExternalProduct() {
        return createProducts(1L);
    }

    public Mono<Product> getExternalProduct(Long id) {
        return createProducts(1L)
                .filter(product -> product.getId().equals(id))
                .next();
    }

    private Flux<Product> createProducts(Long orderId) {
        return orderService.getOrderWithProducts(orderId)
                .flatMapMany(order -> {
                    Product productA = new Product(1L, "Product A", order.getTotalValue() > 100 ? 120.0 : 100.0, order.getId());
                    Product productB = new Product(2L, "Product B", order.getTotalValue() > 50 ? 60.0 : 50.5, order.getId());
                    Product productC = new Product(3L, "Product C", 75.0, order.getId());

                    String message = "Pedido " + order.getId() + " criado com os produtos: " +
                            productA.getName() + ", " + productB.getName() + ", " + productC.getName();
                    kafkaTemplate.send("order-topic", message);

                    return Flux.just(productA, productB, productC)
                            .flatMap(product -> productRepository.save(product));
                })
                .switchIfEmpty(Flux.empty());
    }
}
