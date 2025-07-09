package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.DataProducts;
import com.ecommerce.aplication.records.DataProductsResponse;
import com.ecommerce.aplication.services.ServiceProducts;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<DataProductsResponse> create(@RequestBody DataProducts data) {
        DataProductsResponse created = service.create(data);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataProductsResponse> update(@PathVariable Long id, @RequestBody DataProducts data) {
        DataProductsResponse updated = service.update(id, data);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataProductsResponse> findById(@PathVariable Long id) {
        DataProductsResponse product = service.findById(id);
        return ResponseEntity.ok(product);

    }

    @GetMapping
    public ResponseEntity<List<DataProductsResponse>> findAll(@RequestParam(defaultValue = "0") int page) {
        List<DataProductsResponse> products = service.findAll(page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/buscarItem")
    public ResponseEntity<List<DataProductsResponse>> findByItem(
            @RequestParam CategoryItem item,
            @RequestParam(defaultValue = "0") int page) {
        List<DataProductsResponse> products = service.findByItem(item, page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> checkIfProductExists(
            @RequestParam String name,
            @RequestParam String color,
            @RequestParam String size
    ) {
        boolean exists = service.existsByNameAndColorAndSize(name, color, size);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/buscarTipo")
    public ResponseEntity<List<DataProductsResponse>> findByType(
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        List<DataProductsResponse> products = service.findByType(type, page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/buscarItemTipo")
    public ResponseEntity<List<DataProductsResponse>> findByItemAndType(
            @RequestParam CategoryItem item,
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        List<DataProductsResponse> products = service.findByItemAndType(item, type, page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/buscarTamanho")
    public ResponseEntity<List<DataProductsResponse>> getBySize(
            @RequestParam String tamanho,
            @RequestParam(defaultValue = "0") int page
    ) {
        List<DataProductsResponse> products = service.findBySize(tamanho, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarCor")
    public ResponseEntity<List<DataProductsResponse>> getByColor(
            @RequestParam String cor,
            @RequestParam(defaultValue = "0") int page
    ) {
        List<DataProductsResponse> products = service.findByColor(cor, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarNome")
    public ResponseEntity<List<DataProductsResponse>> getByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page
    ) {
        List<DataProductsResponse> products = service.findByNameContaining(name, page).getContent();
        return ResponseEntity.ok(products);
    }


    @GetMapping("/ordenar")
    public ResponseEntity<List<DataProductsResponse>> getAllOrderByPrice(
            @RequestParam(defaultValue = "asc") String priceSort,
            @RequestParam(defaultValue = "0") int page
    ) {
        List<DataProductsResponse> products = service.findAllOrderByPrice(priceSort, page).getContent();
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
