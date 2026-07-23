package com.senhorcafe.queuecart.order.adapter.in.web;

import com.senhorcafe.queuecart.order.adapter.in.web.dto.CreateOrderDTO;
import com.senhorcafe.queuecart.order.adapter.in.web.dto.OrderDTO;
import com.senhorcafe.queuecart.order.adapter.in.web.dto.OrderItemDTO;
import com.senhorcafe.queuecart.order.adapter.in.web.dto.OrderItemRequestDTO;
import com.senhorcafe.queuecart.order.application.OrderService;
import com.senhorcafe.queuecart.order.domain.Order;
import com.senhorcafe.queuecart.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("order/")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("create-order")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        List<OrderItem> items = createOrderDTO.items().stream()
                .map(this::toDomainItem)
                .toList();

        Order order = orderService.createOrder(createOrderDTO.userId(), items);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(order));
    }

    @GetMapping("all-orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders().stream().map(this::toDTO).toList());
    }

    @GetMapping("order/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(orderService.getOrder(id)));
    }

    @GetMapping("order/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId).stream().map(this::toDTO).toList());
    }

    @PatchMapping("order/{id}/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(orderService.confirmOrder(id)));
    }

    @PatchMapping("order/{id}/ship")
    public ResponseEntity<OrderDTO> shipOrder(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(orderService.shipOrder(id)));
    }

    @PatchMapping("order/{id}/deliver")
    public ResponseEntity<OrderDTO> deliverOrder(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(orderService.deliverOrder(id)));
    }

    @PatchMapping("order/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(orderService.cancelOrder(id)));
    }

    @DeleteMapping("delete-order/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    private OrderItem toDomainItem(OrderItemRequestDTO dto) {
        return new OrderItem(dto.productId(), dto.productName(), dto.unitPrice(), dto.quantity());
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> new OrderItemDTO(item.productId(), item.productName(), item.unitPrice(), item.quantity(), item.subtotal()))
                .toList();

        return new OrderDTO(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                items,
                order.totalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
