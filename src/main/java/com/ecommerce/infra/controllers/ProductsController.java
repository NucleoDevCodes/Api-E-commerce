package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.ProductsRecords.DataProducts;
import com.ecommerce.aplication.records.ProductsRecords.DataProductsResponse;
import com.ecommerce.aplication.services.ServiceProducts;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private static final Logger logger = LoggerFactory.getLogger(ProductsController.class);

    private final ServiceProducts service;

    public ProductsController(ServiceProducts service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DataProductsResponse> create(@RequestBody DataProducts data) {
        logger.info("Criando Produto com nome: {}", data.name());
        DataProductsResponse created = service.create(data);
        logger.info("Produto Criado com ID: {}", created.id());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataProductsResponse> update(@PathVariable Long id, @RequestBody DataProducts data) {
        logger.info("Atualizando Produto com ID: {}", id);
        DataProductsResponse updated = service.update(id, data);
        logger.info("Produto Atualizado com ID: {}", updated.id());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataProductsResponse> findById(@PathVariable Long id) {
        logger.info("Buscando Produto com o  ID: {}", id);
        DataProductsResponse product = service.findById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<DataProductsResponse>> findAll(@RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando todos os Produtos,pagina: {}", page);
        List<DataProductsResponse> products = service.findAll(page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarItem")
    public ResponseEntity<List<DataProductsResponse>> findByItem(
            @RequestParam CategoryItem item,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando todos os Produtos por Item: {}, pagina: {}", item, page);
        List<DataProductsResponse> products = service.findByItem(item, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> checkIfProductExists(
            @RequestParam String name,
            @RequestParam String color,
            @RequestParam String size
    ) {
        logger.info("Verificando se Produto existe com  - Nome: {}, Cor: {}, Tamanho: {}", name, color, size);
        boolean exists = service.existsByNameAndColorAndSize(name, color, size);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/buscarTipo")
    public ResponseEntity<List<DataProductsResponse>> findByType(
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produto por Tipo: {}, pagina: {}", type, page);
        List<DataProductsResponse> products = service.findByType(type, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarItemTipo")
    public ResponseEntity<List<DataProductsResponse>> findByItemAndType(
            @RequestParam CategoryItem item,
            @RequestParam CategoryType type,
            @RequestParam(defaultValue = "0") int page) {
        logger.info("Buscando Produto por Item: {} e Tipo: {}, pagina: {}", item, type, page);
        List<DataProductsResponse> products = service.findByItemAndType(item, type, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarTamanho")
    public ResponseEntity<List<DataProductsResponse>> getBySize(
            @RequestParam String tamanho,
            @RequestParam(defaultValue = "0") int page
    ) {
        logger.info("Buscando Produto por Tamanho: {}, pagina: {}", tamanho, page);
        List<DataProductsResponse> products = service.findBySize(tamanho, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarCor")
    public ResponseEntity<List<DataProductsResponse>> getByColor(
            @RequestParam String cor,
            @RequestParam(defaultValue = "0") int page
    ) {
        logger.info("Buscando Produto por Cor: {}, pagina: {}", cor, page);
        List<DataProductsResponse> products = service.findByColor(cor, page).getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/buscarNome")
    public ResponseEntity<List<DataProductsResponse>> getByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page
    ) {
        logger.info("Buscando Produto com Nome: {}, pagina: {}", name, page);
        List<DataProductsResponse> products = service.findByNameContaining(name, page).getContent();
        return ResponseEntity.ok(products);
    }


    @GetMapping("/ordenar")
    public ResponseEntity<List<DataProductsResponse>> getAllOrderByPrice(
            @RequestParam(defaultValue = "asc") String priceSort,
            @RequestParam(defaultValue = "0") int page
    ) {
        logger.info("Buscando Produto com ordem de Preços: {}, pagina: {}", priceSort, page);
        List<DataProductsResponse> products = service.findAllOrderByPrice(priceSort, page).getContent();
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deletando Produto com ID: {}", id);
        service.delete(id);
        logger.info("Produto Deletado com ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
