package com.management.repository.impl;

import com.management.model.Order;
import com.management.repository.OrderRepositoryCustom;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final DatabaseClient databaseClient;

    public OrderRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<Order> findAllWithPagination(int offset, int size) {
        return databaseClient.sql("SELECT * FROM orders ORDER BY id LIMIT :limit OFFSET :offset")
                .bind("limit", size)
                .bind("offset", offset)
                .map(row -> new Order(
                        row.get("id", Long.class),
                        row.get("customer", String.class),
                        row.get("status", String.class),
                        row.get("totalValue", Double.class)
                ))
                .all();
    }
}
