package com.senhorcafe.queuecart.order.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, Long> {
    Page<OrderJpaEntity> findByUserId(Long userId, Pageable pageable);
}
