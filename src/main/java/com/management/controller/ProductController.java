package com.management.controller;

import com.management.model.Product;
import com.management.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/external")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/{id}")
    public Mono<Product> getExternalProduct(@PathVariable Long id) {
        return productService.getExternalProduct(id);
    }

    @GetMapping("/products")
    public Flux<Product> getExternalProducts() {
        return productService.getExternalProduct();
    }
}
