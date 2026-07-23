package com.senhorcafe.queuecart.order.adapter.out.persistence;

import com.senhorcafe.queuecart.order.domain.Order;
import com.senhorcafe.queuecart.order.domain.OrderItem;
import com.senhorcafe.queuecart.order.domain.OrderPageRequest;
import com.senhorcafe.queuecart.order.domain.OrderPageResult;
import com.senhorcafe.queuecart.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class OrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = order.getId() != null
                ? springDataOrderRepository.findById(order.getId())
                        .orElseThrow(() -> new IllegalStateException("Order not found: " + order.getId()))
                : new OrderJpaEntity();

        entity.setUserId(order.getUserId());
        entity.setStatus(order.getStatus());
        entity.setItems(new ArrayList<>(order.getItems().stream().map(this::toEmbeddable).toList()));

        OrderJpaEntity saved = springDataOrderRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return springDataOrderRepository.findById(id).map(this::toDomain);
    }

    @Override
    public OrderPageResult<Order> findAll(OrderPageRequest pageRequest) {
        return toPageResult(springDataOrderRepository.findAll(toPageable(pageRequest)));
    }

    @Override
    public OrderPageResult<Order> findByUserId(Long userId, OrderPageRequest pageRequest) {
        return toPageResult(springDataOrderRepository.findByUserId(userId, toPageable(pageRequest)));
    }

    @Override
    public void deleteById(Long id) {
        springDataOrderRepository.deleteById(id);
    }

    private Pageable toPageable(OrderPageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private OrderPageResult<Order> toPageResult(Page<OrderJpaEntity> page) {
        return new OrderPageResult<>(
                page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private OrderItemEmbeddable toEmbeddable(OrderItem item) {
        OrderItemEmbeddable embeddable = new OrderItemEmbeddable();
        embeddable.setProductId(item.productId());
        embeddable.setProductName(item.productName());
        embeddable.setUnitPrice(item.unitPrice());
        embeddable.setQuantity(item.quantity());
        return embeddable;
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(e -> new OrderItem(e.getProductId(), e.getProductName(), e.getUnitPrice(), e.getQuantity()))
                .toList();
        return Order.restore(entity.getId(), entity.getUserId(), entity.getStatus(), items, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
