package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.ProductsRecords.DataProducts;
import com.ecommerce.aplication.records.ProductsRecords.DataProductsResponse;
import com.ecommerce.aplication.records.ProductsRecords.DataUpdateQuantRequest;
import com.ecommerce.aplication.services.ServiceProductsWrite;
import com.ecommerce.model.product.ProductModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductsControllerWrite {

    private static final Logger logger = LoggerFactory.getLogger(ProductsControllerWrite.class);
    private  final ServiceProductsWrite service;

    public ProductsControllerWrite(ServiceProductsWrite service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DataProductsResponse> create(@RequestBody DataProducts data){
        logger.info("Criado Produto com nome: {}"+data.name());
        DataProductsResponse created=service.create(data);
        logger.info("Produto Criado com ID: {}"+ created.id());
        return  new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public  ResponseEntity<DataProductsResponse> update(@PathVariable("id") Long id, @RequestBody DataProducts data){
        logger.info("Atualizando Produto com ID: {}"+ id);
        DataProductsResponse updated=service.update(id,data);
        logger.info("Produto Atualizado com ID: {}", updated.id());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<?> updateQuant(@PathVariable Long id, @RequestBody DataUpdateQuantRequest request) {
        logger.info("Atualizando estoque do produto ID: {}", id);
        ProductModel product = service.repository.findById(id).orElse(null);

        if (product == null) {
            logger.warn("Produto ID {} não encontrado para atualização de estoque", id);
            return ResponseEntity.notFound().build();
        }

        if (request.quant() < 0) {
            return ResponseEntity.badRequest().body("Quantidade de estoque não pode ser negativa.");
        }

        product.setQuant(request.quant());
        service.save(product);

        logger.info("Estoque atualizado para produto ID: {}, novo estoque: {}", id, request.quant());
        return ResponseEntity.ok("Estoque atualizado com sucesso.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deletando Produto com ID: {}", id);
        service.delete(id);
        logger.info("Produto Deletado com ID: {}", id);
        return ResponseEntity.noContent().build();
    }

}
