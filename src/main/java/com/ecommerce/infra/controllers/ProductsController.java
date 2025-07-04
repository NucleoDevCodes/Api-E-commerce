package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.DataProducts;
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
    public ResponseEntity<ProductModel> create(@RequestBody DataProducts data) {
        ProductModel created = service.create(data);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> update(@PathVariable Long id, @RequestBody DataProducts data) {
        ProductModel updated = service.update(id, data);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> findById(@PathVariable Long id) {
        ProductModel product = service.findById(id);
        return ResponseEntity.ok(product);

    }

    @GetMapping
    public ResponseEntity<List<ProductModel>> findAll(@RequestParam(defaultValue = "0") int page) {
        List<ProductModel> products = service.findAll(page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/buscarItem")
    public ResponseEntity<List<ProductModel>> findByItem(
            @RequestParam CategoryItem item,
            @RequestParam(defaultValue = "0") int page) {
        List<ProductModel> products = service.findByItem(item, page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/buscarTipo")
    public ResponseEntity<List<ProductModel>> findByType(
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        List<ProductModel> products = service.findByType(type, page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/buscarItemTipo")
    public ResponseEntity<List<ProductModel>> findByItemAndType(
            @RequestParam CategoryItem item,
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        List<ProductModel> products = service.findByItemAndType(item, type, page).getContent();
        return ResponseEntity.ok(products);

    }

    @GetMapping("/buscarTamanho")
    public Page<ProductModel> getBySize(
            @RequestParam String tamanho,
            @RequestParam(defaultValue = "0") int page
    ) {
        return service.findBySize(tamanho, page);
    }

    @GetMapping("/buscarCor")
    public Page<ProductModel> getByColor(
            @RequestParam String cor,
            @RequestParam(defaultValue = "0") int page
    ) {
        return service.findByColor(cor, page);
    }

    @GetMapping("/buscarNome")
    public Page<ProductModel> getByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page
    ) {
        return service.findByNameContaining(name, page);
    }

    @GetMapping("/ordenar")
    public Page<ProductModel> getAllOrderByPrice(
            @RequestParam(defaultValue = "asc") String priceSort,
            @RequestParam(defaultValue = "0") int page
    ) {
        return service.findAllOrderByPrice(priceSort, page);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
