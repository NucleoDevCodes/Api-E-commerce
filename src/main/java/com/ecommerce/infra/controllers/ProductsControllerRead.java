package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.ProductsRecords.DataProductsResponse;
import com.ecommerce.aplication.services.ServiceProductRead;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsControllerRead {

    private static final Logger logger = LoggerFactory.getLogger(ProductsControllerRead.class);

    private final ServiceProductRead service;

    public ProductsControllerRead(ServiceProductRead service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataProductsResponse> findById(@PathVariable Long id) {
        logger.info("Buscando Produto com o ID: {}", id);
        DataProductsResponse product = service.findById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<DataProductsResponse>> findAll(@RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando todos os Produtos, página: {}", page);
        List<DataProductsResponse> products = service.findAll(page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarItem")
    public ResponseEntity<List<DataProductsResponse>> findByItem(
            @RequestParam CategoryItem item,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produtos por Item: {}, página: {}", item, page);
        List<DataProductsResponse> products = service.findByItem(item, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarTipo")
    public ResponseEntity<List<DataProductsResponse>> findByType(
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produtos por Tipo: {}, página: {}", type, page);
        List<DataProductsResponse> products = service.findByType(type, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarItemTipo")
    public ResponseEntity<List<DataProductsResponse>> findByItemAndType(
            @RequestParam CategoryItem item,
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produtos por Item: {} e Tipo: {}, página: {}", item, type, page);
        List<DataProductsResponse> products = service.findByItemAndType(item, type, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarTamanho")
    public ResponseEntity<List<DataProductsResponse>> getBySize(
            @RequestParam String tamanho,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produtos por Tamanho: {}, página: {}", tamanho, page);
        List<DataProductsResponse> products = service.findBySize(tamanho, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarCor")
    public ResponseEntity<List<DataProductsResponse>> getByColor(
            @RequestParam String cor,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produtos por Cor: {}, página: {}", cor, page);
        List<DataProductsResponse> products = service.findByColor(cor, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarNome")
    public ResponseEntity<List<DataProductsResponse>> getByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produtos com Nome: {}, página: {}", name, page);
        List<DataProductsResponse> products = service.findByNameContaining(name, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/ordenar")
    public ResponseEntity<List<DataProductsResponse>> getAllOrderByPrice(
            @RequestParam(defaultValue = "asc") String priceSort,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produtos ordenados por Preço: {}, página: {}", priceSort, page);
        List<DataProductsResponse> products = service.findAllOrderByPrice(priceSort, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> checkIfProductExists(
            @RequestParam String name,
            @RequestParam String color,
            @RequestParam String size) {
        logger.info("Verificando se Produto existe - Nome: {}, Cor: {}, Tamanho: {}", name, color, size);
        boolean exists = service.existsByNameAndColorAndSize(name, color, size);
        return ResponseEntity.ok(exists);
    }
}
