package com.management.service;

import com.management.dto.OrderDTO;
import com.management.dto.ProductDTO;
import com.management.model.Order;
import com.management.model.Product;
import com.management.repository.OrderRepository;
import com.management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TotalValueCalculatorService totalValueCalculatorService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        TotalValueCalculatorService totalValueCalculatorService, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.totalValueCalculatorService = totalValueCalculatorService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    //@KafkaListener(topics = "order-topic", groupId = "order-consumer-group")
    public void consumeOrder(String orderMessage) {
        try {
            OrderDTO orderDTO = objectMapper.readValue(orderMessage, OrderDTO.class);
            createOrder(orderDTO)
                    .doOnTerminate(() -> {
                        sendOrderToExternalB(orderDTO);
                    })
                    .subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Mono<OrderDTO> createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setCustomer(orderDTO.getCustomer());
        order.setStatus(orderDTO.getStatus());

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    orderDTO.getProducts().forEach(productDTO -> {
                        Product product = new Product();
                        product.setName(productDTO.getName());
                        product.setPrice(productDTO.getPrice());
                        product.setOrderId(savedOrder.getId());
                        savedOrder.addProduct(product);
                    });

                    return productRepository.saveAll(savedOrder.getProducts())
                            .collectList()
                            .flatMap(savedProducts -> calculateTotalValue(savedOrder.getId()))
                            .map(updatedOrder -> new OrderDTO(savedOrder));
                });
    }

    public Mono<OrderDTO> calculateTotalValue(Long orderId) {
        return productRepository.findByOrderId(orderId)
                .buffer(1000)
                .flatMap(productsBatch -> {
                    double batchTotal = productsBatch.stream().mapToDouble(Product::getPrice).sum();
                    return orderRepository.findById(orderId)
                            .flatMap(order -> {
                                order.setTotalValue(order.getTotalValue() + batchTotal);
                                return orderRepository.save(order)
                                        .map(savedOrder -> new OrderDTO(savedOrder));
                            });
                })
                .reduce((order1, order2) -> order2);
    }

    private void sendOrderToExternalB(OrderDTO orderDTO) {
        try {
            String orderMessage = objectMapper.writeValueAsString(orderDTO);
            kafkaTemplate.send("external-b-order-topic", orderMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Flux<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .flatMap(order ->
                        productRepository.findByOrderId(order.getId())
                                .collectList()
                                .map(products -> {
                                    order.setProducts(products);
                                    return new OrderDTO(order);
                                })
                );
    }

    public Mono<OrderDTO> getOrderWithProducts(Long orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    return productRepository.findByOrderId(orderId)
                            .collectList()
                            .map(products -> {
                                products.forEach(product -> product.setOrderId(orderId));
                                order.setProducts(products);
                                return new OrderDTO(order);
                            });
                });
    }

    public Mono<OrderDTO> addProductToOrder(Long orderId, ProductDTO productDTO) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    Product product = new Product();
                    product.setName(productDTO.getName());
                    product.setPrice(productDTO.getPrice());
                    product.setOrderId(order.getId());

                    return productRepository.save(product)
                            .flatMap(savedProduct -> {
                                order.addProduct(savedProduct);
                                return orderRepository.save(order)
                                        .map(savedOrder -> new OrderDTO(savedOrder));
                            });
                });
    }
}
