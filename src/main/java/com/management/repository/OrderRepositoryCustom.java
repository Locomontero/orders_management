package com.management.repository;

import com.management.model.Order;
import reactor.core.publisher.Flux;

public interface OrderRepositoryCustom {
    Flux<Order> findAllWithPagination(int offset, int size);
}
