package com.senhorcafe.queuecart.order.application;

import com.senhorcafe.queuecart.order.domain.Order;
import com.senhorcafe.queuecart.order.domain.OrderItem;
import com.senhorcafe.queuecart.order.domain.OrderPageRequest;
import com.senhorcafe.queuecart.order.domain.OrderPageResult;
import com.senhorcafe.queuecart.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public Order createOrder(Long userId, List<OrderItem> items) {
        Order order = Order.create(userId, items);
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return findOrderOrThrow(orderId);
    }

    public OrderPageResult<Order> getAllOrders(OrderPageRequest pageRequest) {
        return orderRepository.findAll(pageRequest);
    }

    public OrderPageResult<Order> getOrdersByUser(Long userId, OrderPageRequest pageRequest) {
        return orderRepository.findByUserId(userId, pageRequest);
    }

    public Order confirmOrder(Long orderId) {
        return applyTransition(orderId, Order::confirm);
    }

    public Order shipOrder(Long orderId) {
        return applyTransition(orderId, Order::ship);
    }

    public Order deliverOrder(Long orderId) {
        return applyTransition(orderId, Order::deliver);
    }

    public Order cancelOrder(Long orderId) {
        return applyTransition(orderId, Order::cancel);
    }

    public void deleteOrder(Long orderId) {
        findOrderOrThrow(orderId);
        orderRepository.deleteById(orderId);
    }

    private Order applyTransition(Long orderId, Consumer<Order> transition) {
        Order order = findOrderOrThrow(orderId);
        try {
            transition.accept(order);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return orderRepository.save(order);
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
