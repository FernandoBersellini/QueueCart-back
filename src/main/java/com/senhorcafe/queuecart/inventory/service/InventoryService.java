package com.senhorcafe.queuecart.inventory.service;

import com.senhorcafe.queuecart.inventory.dto.StockAdjustmentItem;
import com.senhorcafe.queuecart.inventory.dto.StockItemDTO;
import com.senhorcafe.queuecart.inventory.entity.ProcessedOrderEvent;
import com.senhorcafe.queuecart.inventory.entity.StockItem;
import com.senhorcafe.queuecart.inventory.repository.ProcessedOrderEventRepository;
import com.senhorcafe.queuecart.inventory.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final StockItemRepository stockItemRepository;
    private final ProcessedOrderEventRepository processedOrderEventRepository;

    public StockItemDTO getStock(Long productId) {
        return toDTO(findStockItemOrThrow(productId));
    }

    @Transactional
    public void increaseStock(Long productId, int quantity) {
        StockItem stockItem = findStockItemOrThrow(productId);
        stockItem.setQuantityAvailable(stockItem.getQuantityAvailable() + quantity);
        stockItemRepository.save(stockItem);
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        StockItem stockItem = findStockItemOrThrow(productId);
        if (stockItem.getQuantityAvailable() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for product " + productId);
        }
        stockItem.setQuantityAvailable(stockItem.getQuantityAvailable() - quantity);
        stockItemRepository.save(stockItem);
    }

    @Transactional
    public void processOrderStockDecrement(Long orderId, List<StockAdjustmentItem> items) {
        if (processedOrderEventRepository.existsByOrderId(orderId)) {
            return;
        }

        items.forEach(item -> decreaseStock(item.productId(), item.quantity()));

        ProcessedOrderEvent processedOrderEvent = new ProcessedOrderEvent();
        processedOrderEvent.setOrderId(orderId);
        processedOrderEventRepository.save(processedOrderEvent);
    }

    private StockItem findStockItemOrThrow(Long productId) {
        return stockItemRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock item not found for product " + productId));
    }

    private StockItemDTO toDTO(StockItem stockItem) {
        return new StockItemDTO(stockItem.getProductId(), stockItem.getQuantityAvailable());
    }
}
