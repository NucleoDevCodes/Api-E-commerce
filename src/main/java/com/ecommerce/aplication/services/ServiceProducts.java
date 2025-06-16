package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.DataProducts;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceProducts {

    private final ProductRepository repository;

    public ServiceProducts(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductModel create(DataProducts data) {
        boolean exists = repository.existsByNameAndColorAndSize(data.name(), data.color(), data.size());
        if (exists) {
            throw new ResourceNotFoundException("Produto já existente com nome '" + data.name() +
                    "', cor '" + data.color() +
                    "' e tamanho '" + data.size() + "'.");
        }

        ProductModel product = new ProductModel(data);
        return repository.save(product);
    }


    @Transactional
    public ProductModel update(Long id, DataProducts data) {
        ProductModel existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));
        existing.setName(data.name());
        existing.setPrice(data.price());
        existing.setColor(data.color());
        existing.setSize(data.size());
        return repository.save(existing);
    }

    public ProductModel findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));
    }

    public List<ProductModel> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        ProductModel existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));
        repository.delete(existing);
    }
}
