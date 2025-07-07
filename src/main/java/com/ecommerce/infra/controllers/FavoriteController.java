package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.DataFavoriteProductRequest;
import com.ecommerce.aplication.services.ServiceFavoriteProducts;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favoritos")
public class FavoriteController {
    private final ServiceFavoriteProducts service;

    public FavoriteController(ServiceFavoriteProducts service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> add(@AuthenticationPrincipal(expression = "id") Long userId,
                                 @RequestBody DataFavoriteProductRequest request) {
        service.add(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> remove(@AuthenticationPrincipal(expression = "id") Long userId,
                                    @PathVariable("productId") Long productId) {
        service.remove(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok(service.list(userId));
    }
}
