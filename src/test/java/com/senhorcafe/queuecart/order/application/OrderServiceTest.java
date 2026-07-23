package com.senhorcafe.queuecart.order.application;

import com.senhorcafe.queuecart.order.domain.Order;
import com.senhorcafe.queuecart.order.domain.OrderItem;
import com.senhorcafe.queuecart.order.domain.OrderRepository;
import com.senhorcafe.queuecart.order.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
    }

    private List<OrderItem> singleItem() {
        return List.of(new OrderItem(10L, "Espresso Blend", BigDecimal.valueOf(29.90), 2));
    }

    private Order buildOrder(Long id, OrderStatus status) {
        return Order.restore(id, 1L, status, singleItem(), Instant.now(), Instant.now());
    }

    @Test
    void createOrderShouldBuildPendingOrderAndSave() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(1L, singleItem());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(59.80));
    }

    @Test
    void createOrderShouldRejectEmptyItems() {
        assertThatThrownBy(() -> orderService.createOrder(1L, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getOrderShouldReturnOrderWhenFound() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrder(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getOrderShouldThrowWhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllOrdersShouldDelegateToRepository() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByUserShouldDelegateToRepository() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void confirmOrderShouldMoveFromPendingToConfirmed() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.confirmOrder(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shipOrderShouldThrowConflictWhenOrderIsPending() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.shipOrder(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void deliverOrderShouldMoveFromShippedToDelivered() {
        Order order = buildOrder(1L, OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.deliverOrder(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void cancelOrderShouldMoveFromPendingToCancelled() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.cancelOrder(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrderShouldThrowConflictWhenAlreadyDelivered() {
        Order order = buildOrder(1L, OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmOrderShouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.confirmOrder(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteOrderShouldRemoveExistingOrder() {
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteOrderShouldThrowWhenNotFoundAndNeverDelete() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrder(99L))
                .isInstanceOf(ResponseStatusException.class);

        verify(orderRepository, never()).deleteById(any());
    }
}
