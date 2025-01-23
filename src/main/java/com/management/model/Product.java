package com.management.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Table("product")
public class Product {

    @Id
    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    private double price;

    @Column("order_id")
    private Long orderId;

    public Product() {
    }

    public Product(Long id, String name, double price, Long orderId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.orderId = orderId;
    }

    public Product(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
