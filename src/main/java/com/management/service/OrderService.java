package com.management.service;

import com.management.dto.OrderDTO;
import com.management.dto.ProductDTO;
import com.management.model.Order;
import com.management.model.Product;
import com.management.repository.OrderRepository;
import com.management.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
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
                    .doOnTerminate(() -> sendOrderToExternalB(orderDTO))
                    .subscribe();
        } catch (Exception e) {
            logger.error("Erro ao processar o pedido", e);
        }
    }

    private void sendOrderToExternalB(OrderDTO orderDTO) {
        try {
            String orderMessage = objectMapper.writeValueAsString(orderDTO);

            kafkaTemplate.send("external-b-order-topic", orderMessage)
                    .addCallback(
                            result -> logger.info("SUCESS!"),
                            ex -> {
                                logger.error("Erro ao enviar para External B, tentando novamente...", ex);
                                retrySendOrderToExternalB(orderDTO);
                            }
                    );
        } catch (Exception e) {
            logger.error("Erro converter JSON", e);
        }
    }

    private void retrySendOrderToExternalB(OrderDTO orderDTO) {
        try {

            String orderMessage = objectMapper.writeValueAsString(orderDTO);
            kafkaTemplate.send("external-b-order-topic", orderMessage);
            logger.info("Retry de envio para External B bem-sucedido!");
        } catch (Exception e) {
            logger.error("Falha no retry de envio para External B", e);
        }
    }


    @Transactional
    public Mono<OrderDTO> createOrder(OrderDTO orderDTO) {

        Order order = new Order();
        order.setCustomer(orderDTO.getCustomer());
        order.setStatus(orderDTO.getStatus());
        order.setTotalValue(0.0);

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    return Flux.fromIterable(orderDTO.getProducts())
                            .filter(productDTO -> productDTO.getPrice() > 0 && productDTO.getName() != null && !productDTO.getName().isEmpty())
                            .map(productDTO -> {
                                Product product = new Product();
                                product.setName(productDTO.getName());
                                product.setPrice(productDTO.getPrice());
                                product.setOrderId(savedOrder.getId());
                                return product;
                            })
                            .collectList()
                            .flatMap(products -> {

                                savedOrder.setProducts(products);
                                return orderRepository.save(savedOrder);
                            })
                            .map(savedOrderAfterSave -> new OrderDTO(savedOrderAfterSave));
                });
    }

    @Transactional
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

    @Transactional
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

    @Transactional
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
