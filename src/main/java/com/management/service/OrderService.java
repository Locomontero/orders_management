package com.management.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.management.dto.OrderDTO;
import com.management.dto.ProductDTO;
import com.management.model.Order;
import com.management.model.Product;
import com.management.repository.OrderRepository;
import com.management.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private DatabaseClient databaseClient;
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

    @KafkaListener(topics = "order-topic", groupId = "order-consumer-group")
    public void consumeOrder(String orderMessage) {
        try {
            List<OrderDTO> orderDTOList = objectMapper.readValue(orderMessage, new TypeReference<List<OrderDTO>>(){});

            for (OrderDTO orderDTO : orderDTOList) {
                createOrderWithProducts(orderDTO).subscribe();
            }
        } catch (Exception e) {
            logger.error("Erro ao processar os pedidos", e);
        }
    }
@Transactional
public Mono<Long> createOrderWithProducts(OrderDTO orderDTO) {

    Order order = convertToOrder(orderDTO);


    return databaseClient.sql("INSERT INTO orders (customer, status, total_value) VALUES (:customer, :status, 0) RETURNING id")
            .bind("customer", order.getCustomer())
            .bind("status", order.getStatus())
            .fetch()
            .one()
            .map(row -> (Long) row.get("id"))
            .flatMap(orderId -> {

                return insertProductsInBatch(order.getProducts(), orderId)
                        .then(updateTotalValue(orderId))
                        .thenReturn(orderId);
            });
}

    private Mono<Void> insertProductsInBatch(List<Product> products, Long orderId) {
        StringBuilder sql = new StringBuilder("INSERT INTO product (name, price, order_id) VALUES ");
        for (int i = 0; i < products.size(); i++) {
            sql.append("(:name" + i + ", :price" + i + ", :orderId)");
            if (i < products.size() - 1) {
                sql.append(", ");
            }
        }

        var query = databaseClient.sql(sql.toString());

        for (int i = 0; i < products.size(); i++) {
            query = query.bind("name" + i, products.get(i).getName())
                    .bind("price" + i, products.get(i).getPrice())
                    .bind("orderId", orderId);
        }

        return query.fetch().rowsUpdated().then();
    }

    private Mono<Void> updateTotalValue(Long orderId) {
        String sql = "UPDATE orders SET total_value = (SELECT SUM(price) FROM product WHERE order_id = :orderId) WHERE id = :orderId";

        return databaseClient.sql(sql)
                .bind("orderId", orderId)
                .fetch()
                .rowsUpdated()
                .then();
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

    private Order convertToOrder(OrderDTO orderDTO) {
        List<Product> products = orderDTO.getProducts().stream()
                .map(productDTO -> new Product(productDTO.getId(), productDTO.getName(), productDTO.getPrice()))
                .collect(Collectors.toList());
        return new Order(null, orderDTO.getCustomer(), orderDTO.getStatus(), orderDTO.getTotalValue(), products);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        List<ProductDTO> productDTOs = order.getProducts().stream()
                .map(product -> new ProductDTO(product))
                .collect(Collectors.toList());
        return new OrderDTO(order.getId(), order.getCustomer(), order.getStatus(), order.getTotalValue(), productDTOs);
    }

}
