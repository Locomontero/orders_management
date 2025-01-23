package com.management.dto;

import com.management.model.Product;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ProductDTO {

    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    private double price;

    private Long orderId;

    public ProductDTO() {}

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.orderId = product.getOrderId();
    }
}
