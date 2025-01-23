package com.management.service;

import com.management.model.Product;
import com.management.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ProductService(OrderService orderService, ProductRepository productRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Product> getExternalProduct(Long id) {
        return getProductsForOrder(id)
                .filter(product -> product.getId().equals(id))
                .next();
    }

    private Flux<Product> getProductsForOrder(Long orderId) {
        return orderService.getOrderWithProducts(orderId)
                .flatMapMany(order -> {
                    String message = "Pedido " + order.getId() + " com os produtos: " +
                            order.getProducts().stream()
                                    .map(product -> product.getName())
                                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);

                    kafkaTemplate.send("order-topic", message)
                            .addCallback(
                                    result -> logger.info("Kafka message ok"),
                                    ex -> logger.error("Erro- Kafka", ex)
                            );

                    return Flux.fromIterable(order.getProducts())
                            .map(productDTO -> new Product(productDTO.getId(), productDTO.getName(), productDTO.getPrice(), productDTO.getOrderId()));
                })
                .switchIfEmpty(Flux.empty())
                .onBackpressureBuffer();
    }

}
