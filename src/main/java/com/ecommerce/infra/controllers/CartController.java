package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.CartRecords.DataCartItemRequest;
import com.ecommerce.aplication.records.CartRecords.DataCartItemResponse;
import com.ecommerce.aplication.services.ServiceCart;
import com.ecommerce.model.users.Users;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carrinho")
public class CartController {

    private final ServiceCart cartService;

    public CartController(ServiceCart cartService) {
        this.cartService = cartService;
    }


    @PostMapping("/itens")
    public ResponseEntity<Void> add(@AuthenticationPrincipal Users user, @Valid @RequestBody DataCartItemRequest dto) {
        cartService.addItemToCart(user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/itens/{produtoId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Users user,
                                       @PathVariable("produtoId") Long produtoId) {
        cartService.removeItemCart(user.getId(), produtoId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public ResponseEntity<List<DataCartItemResponse>> get(@AuthenticationPrincipal Users user) {
        var items = cartService.getCartItems(user.getId());
        return ResponseEntity.ok(items);
    }
}
