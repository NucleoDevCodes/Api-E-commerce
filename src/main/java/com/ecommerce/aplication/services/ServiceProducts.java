package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.DataProducts;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceProducts {

    private final ProductRepository repository;

    private static final List<String> ALLOWED_SIZES = List.of("PP", "P", "M", "G", "GG", "U");
    private static final List<String> ALLOWED_COLORS = List.of(
            "PRETO", "BRANCO", "AZUL", "VERMELHO", "CINZA", "VERDE", "ROSA", "AMARELO"
    );

    public ServiceProducts(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductModel create(DataProducts data) {
        String normalizedColor = normalize(data.color());
        String normalizedSize = normalize(data.size());

        validateProductData(data.name(), data.price(), normalizedColor, normalizedSize);

        boolean exists = repository.existsByNameAndColorAndSize(
                data.name(), normalizedColor, normalizedSize
        );
        if (exists) {
            throw new IllegalArgumentException("Produto já existente com nome '" + data.name() +
                    "', cor '" + normalizedColor + "' e tamanho '" + normalizedSize + "'.");
        }

        ProductModel product = new ProductModel(data);
        product.setColor(normalizedColor);
        product.setSize(normalizedSize);

        return repository.save(product);
    }

    @Transactional
    public ProductModel update(Long id, DataProducts data) {
        String normalizedColor = normalize(data.color());
        String normalizedSize = normalize(data.size());

        validateProductData(data.name(), data.price(), normalizedColor, normalizedSize);

        ProductModel existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));

        existing.setName(data.name());
        existing.setPrice(data.price());
        existing.setColor(normalizedColor);
        existing.setSize(normalizedSize);
        existing.setItem(data.item());
        existing.setType(data.type());

        return repository.save(existing);
    }

    public ProductModel findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));
    }

    public Page<ProductModel> findAll(int page) {
        return repository.findAll(PageRequest.of(page, 5));
    }

    public Page<ProductModel> findByItem(CategoryItem item, int page) {
        return repository.findByItem(item, PageRequest.of(page, 5));
    }

    public Page<ProductModel> findByType(CategoryType type, int page) {
        return repository.findByType(type, PageRequest.of(page, 5));
    }

    public Page<ProductModel> findByItemAndType(CategoryItem item, CategoryType type, int page) {
        return repository.findByItemAndType(item, type, PageRequest.of(page, 5));
    }

    @Transactional
    public void delete(Long id) {
        ProductModel existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));
        repository.delete(existing);
    }

    private void validateProductData(String name, Number price, String color, String size) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (price == null || price.doubleValue() <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }

        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("A cor é obrigatória.");
        }

        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException("O tamanho é obrigatório.");
        }

        if (!ALLOWED_COLORS.contains(color)) {
            throw new IllegalArgumentException("Cor '" + color + "' inválida. Cores permitidas: " + ALLOWED_COLORS);
        }

        if (!ALLOWED_SIZES.contains(size)) {
            throw new IllegalArgumentException("Tamanho '" + size + "' inválido. Tamanhos permitidos: " + ALLOWED_SIZES);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.toUpperCase().trim();
    }
}
