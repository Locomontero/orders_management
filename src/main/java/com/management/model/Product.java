package com.management.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
@Entity
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    private double price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public Product(Long id, String name, double price, Order order) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.order = order;
    }

    public Product(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
