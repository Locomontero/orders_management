package com.management.dto;

import com.management.model.Order;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDTO {

    private Long id;
    private String customer;
    private String status;
    private double totalValue;
    private List<ProductDTO> products;

    public OrderDTO() {
    }

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.customer = order.getCustomer();
        this.status = order.getStatus();
        this.totalValue = order.getTotalValue();
        this.products = order.getProducts().stream()
                .map(product -> new ProductDTO(product))
                .collect(Collectors.toList());
    }


}

