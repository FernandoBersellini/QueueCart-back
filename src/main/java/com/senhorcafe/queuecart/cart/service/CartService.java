package com.senhorcafe.queuecart.cart.service;

import com.senhorcafe.queuecart.cart.dto.CartDTO;
import com.senhorcafe.queuecart.cart.dto.CartItemDTO;
import com.senhorcafe.queuecart.cart.entity.Cart;
import com.senhorcafe.queuecart.cart.entity.CartItem;
import com.senhorcafe.queuecart.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    public CartDTO getCart(Long userId) {
        return toDTO(getOrCreateCart(userId));
    }

    public CartDTO addItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> items.add(buildItem(productId, quantity))
                );

        cart.setItems(items);
        return toDTO(cartRepository.save(cart));
    }

    public CartDTO updateItemQuantity(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        Cart cart = findCartOrThrow(userId);
        List<CartItem> items = new ArrayList<>(cart.getItems());
        CartItem item = findItemOrThrow(items, productId);
        item.setQuantity(quantity);

        cart.setItems(items);
        return toDTO(cartRepository.save(cart));
    }

    public CartDTO removeItem(Long userId, Long productId) {
        Cart cart = findCartOrThrow(userId);
        List<CartItem> items = new ArrayList<>(cart.getItems());
        findItemOrThrow(items, productId);
        items.removeIf(item -> item.getProductId().equals(productId));

        cart.setItems(items);
        return toDTO(cartRepository.save(cart));
    }

    public CartDTO clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.setItems(new ArrayList<>());
        return toDTO(cartRepository.save(cart));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
            return cartRepository.save(cart);
        });
    }

    private Cart findCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    private CartItem findItemOrThrow(List<CartItem> items, Long productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));
    }

    private CartItem buildItem(Long productId, int quantity) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private CartDTO toDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(item -> new CartItemDTO(item.getProductId(), item.getQuantity()))
                .toList();

        return new CartDTO(cart.getId(), cart.getUserId(), items);
    }
}
