package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.CartRecords.DataCartItemRequest;
import com.ecommerce.aplication.records.CartRecords.DataCartItemResponse;
import com.ecommerce.aplication.services.ServiceCart;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carrinho")
public class CartController {

    private final ServiceCart cartService;

    public CartController(ServiceCart cartService) {
        this.cartService = cartService;
    }

    private Long getLoggedUserId() {
        return 1L;
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addItem(@RequestBody DataCartItemRequest dto) {
        cartService.addItemToCart(getLoggedUserId(), dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable("productId") Long productId) {
        cartService.removeItemCart(getLoggedUserId(), productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DataCartItemResponse>> getCart() {
        var items = cartService.getCartItems(getLoggedUserId());
        return ResponseEntity.ok(items);
    }
}
