package com.senhorcafe.queuecart.cart.controller;

import com.senhorcafe.queuecart.cart.dto.AddCartItemDTO;
import com.senhorcafe.queuecart.cart.dto.CartDTO;
import com.senhorcafe.queuecart.cart.dto.UpdateCartItemDTO;
import com.senhorcafe.queuecart.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cart/")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("{userId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("add-item/{userId}")
    public ResponseEntity<CartDTO> addItem(@PathVariable Long userId, @RequestBody AddCartItemDTO addCartItemDTO) {
        return ResponseEntity.ok(cartService.addItem(userId, addCartItemDTO.productId(), addCartItemDTO.quantity()));
    }

    @PatchMapping("update-item/{userId}/{productId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestBody UpdateCartItemDTO updateCartItemDTO
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, productId, updateCartItemDTO.quantity()));
    }

    @DeleteMapping("remove-item/{userId}/{productId}")
    public ResponseEntity<CartDTO> removeItem(@PathVariable Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping("clear-cart/{userId}")
    public ResponseEntity<CartDTO> clearCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.clearCart(userId));
    }
}
