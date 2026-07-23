package com.senhorcafe.queuecart.order.domain;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long id);

    OrderPageResult<Order> findAll(OrderPageRequest pageRequest);

    OrderPageResult<Order> findByUserId(Long userId, OrderPageRequest pageRequest);

    void deleteById(Long id);
}
