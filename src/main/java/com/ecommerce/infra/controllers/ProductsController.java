package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.DataProducts;
import com.ecommerce.aplication.services.ServiceProducts;
import com.ecommerce.model.product.ProductModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsController {
    private final ServiceProducts service;

    public ProductsController(ServiceProducts service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductModel> create(@RequestBody DataProducts data) {
        ProductModel created = service.create(data);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> update(@PathVariable("id") Long id, @RequestBody DataProducts data) {
        ProductModel updated = service.update(id, data);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> findById(@PathVariable("id") Long id) {
        ProductModel product = service.findById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductModel>> findAll() {
        List<ProductModel> products = service.findAll();
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
