package com.senhorcafe.queuecart.cart.service;

import com.senhorcafe.queuecart.cart.dto.CartDTO;
import com.senhorcafe.queuecart.cart.entity.Cart;
import com.senhorcafe.queuecart.cart.entity.CartItem;
import com.senhorcafe.queuecart.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository);
    }

    private CartItem buildItem(Long productId, int quantity) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private Cart buildCart(Long id, Long userId, CartItem... items) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>(List.of(items)));
        return cart;
    }

    @Test
    void getCartShouldCreateEmptyCartWhenNoneExists() {
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO result = cartService.getCart(5L);

        assertThat(result.userId()).isEqualTo(5L);
        assertThat(result.items()).isEmpty();
    }

    @Test
    void getCartShouldReturnExistingCart() {
        Cart cart = buildCart(1L, 5L, buildItem(10L, 2));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        CartDTO result = cartService.getCart(5L);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productId()).isEqualTo(10L);
    }

    @Test
    void addItemShouldAppendNewItem() {
        Cart cart = buildCart(1L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO result = cartService.addItem(5L, 10L, 2);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void addItemShouldIncreaseQuantityWhenProductAlreadyInCart() {
        Cart cart = buildCart(1L, 5L, buildItem(10L, 2));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO result = cartService.addItem(5L, 10L, 3);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void addItemShouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> cartService.addItem(5L, 10L, 0))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateItemQuantityShouldChangeExistingItem() {
        Cart cart = buildCart(1L, 5L, buildItem(10L, 2));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO result = cartService.updateItemQuantity(5L, 10L, 8);

        assertThat(result.items().get(0).quantity()).isEqualTo(8);
    }

    @Test
    void updateItemQuantityShouldThrowWhenCartNotFound() {
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity(5L, 10L, 8))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void updateItemQuantityShouldThrowWhenItemNotInCart() {
        Cart cart = buildCart(1L, 5L, buildItem(10L, 2));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItemQuantity(5L, 99L, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void removeItemShouldDeleteExistingItem() {
        Cart cart = buildCart(1L, 5L, buildItem(10L, 2), buildItem(20L, 1));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO result = cartService.removeItem(5L, 20L);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productId()).isEqualTo(10L);
    }

    @Test
    void removeItemShouldThrowWhenItemNotInCart() {
        Cart cart = buildCart(1L, 5L, buildItem(10L, 2));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.removeItem(5L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void clearCartShouldEmptyItems() {
        Cart cart = buildCart(1L, 5L, buildItem(10L, 2), buildItem(20L, 1));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO result = cartService.clearCart(5L);

        assertThat(result.items()).isEmpty();
    }
}
