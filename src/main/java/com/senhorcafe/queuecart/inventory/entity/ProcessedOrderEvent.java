package com.senhorcafe.queuecart.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@Table(name = "processed_order_events")
public class ProcessedOrderEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true)
    private Long orderId;

    @CreationTimestamp
    @Column(name = "processed_at")
    private Instant processedAt;
}
