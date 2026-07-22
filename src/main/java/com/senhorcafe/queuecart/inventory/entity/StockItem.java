package com.senhorcafe.queuecart.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Data
@Table(name = "stock_items")
public class StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity_available")
    private int quantityAvailable;

    @Version
    @Column(name = "version")
    private long version;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
