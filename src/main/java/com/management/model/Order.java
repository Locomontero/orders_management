package com.management.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("orders")
public class Order {

    @Id
    private Long id;

    private String customer;
    private String status;
    private double totalValue;

    private List<Product> products;

    public Order() {
        this.products = new ArrayList<>();
    }

    public Order(Long id, String customer, String status, double totalValue) {
        this.id = id;
        this.customer = customer;
        this.status = status;
        this.totalValue = totalValue;
        this.products = new ArrayList<>();
    }

    public Order(Long id, String customer, String status, double totalValue, List<Product> products) {
        this.id = id;
        this.customer = customer;
        this.status = status;
        this.totalValue = totalValue;
        this.products = products;
    }



    public void addProduct(Product product) {
        this.products.add(product);
    }
}
