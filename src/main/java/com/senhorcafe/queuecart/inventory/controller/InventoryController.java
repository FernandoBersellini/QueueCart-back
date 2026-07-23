package com.senhorcafe.queuecart.inventory.controller;

import com.senhorcafe.queuecart.inventory.dto.StockItemDTO;
import com.senhorcafe.queuecart.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("inventory/")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("stock/{productId}")
    public ResponseEntity<StockItemDTO> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStock(productId));
    }

    @PatchMapping("stock/{productId}/increase")
    public ResponseEntity<Void> increaseStock(@PathVariable Long productId, @RequestParam int quantity) {
        inventoryService.increaseStock(productId, quantity);
        return ResponseEntity.noContent().build();
    }
}
