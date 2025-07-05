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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        List<String> normalizedColors = normalizeList(data.colors());
        List<String> normalizedSizes = normalizeList(data.sizes());

        validateProductData(data.name(), data.price(), normalizedColors, normalizedSizes, data.quant());

        ProductModel product = new ProductModel(data);
        product.setColors(normalizedColors);
        product.setSizes(normalizedSizes);
        product.setQuant(data.quant());

        return repository.save(product);
    }

    @Transactional
    public ProductModel update(Long id, DataProducts data) {
        List<String> normalizedColors = normalizeList(data.colors());
        List<String> normalizedSizes = normalizeList(data.sizes());

        validateProductData(data.name(), data.price(), normalizedColors, normalizedSizes, data.quant());

        ProductModel existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));

        existing.setName(data.name());
        existing.setPrice(data.price());
        existing.setColors(normalizedColors);
        existing.setSizes(normalizedSizes);
        existing.setItem(data.item());
        existing.setType(data.type());
        existing.setQuant(data.quant());

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

    public Page<ProductModel> findByNameContaining(String name, int page) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Termo de busca não pode ser vazio.");
        }
        Pageable pageable = PageRequest.of(page, 5);
        return repository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    public Page<ProductModel> findBySize(String size, int page) {
        String normalizedSize = normalize(size);
        Pageable pageable = PageRequest.of(page, 5);
        return repository.findBySizesContainingIgnoreCase(normalizedSize, pageable);
    }

    public Page<ProductModel> findByColor(String color, int page) {
        String normalizedColor = normalize(color);
        Pageable pageable = PageRequest.of(page, 5);
        return repository.findByColorsContainingIgnoreCase(normalizedColor, pageable);
    }

    public Page<ProductModel> findAllOrderByPrice(String priceSort, int page) {
        Sort sort = "desc".equalsIgnoreCase(priceSort)
                ? Sort.by("price").descending()
                : Sort.by("price").ascending();

        Pageable pageable = PageRequest.of(page, 5, sort);
        return repository.findAll(pageable);
    }

    public boolean existsByNameAndColorAndSize(String name, String color, String size) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }

        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Cor é obrigatória.");
        }

        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException("Tamanho é obrigatório.");
        }

        return repository.existsByNameAndColorAndSize(
                name.trim(),
                color.trim().toUpperCase(),
                size.trim().toUpperCase()
        );
    }

    private List<String> normalizeList(List<String> input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("A lista não pode estar vazia.");
        }

        return input.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(v -> v.trim().toUpperCase())
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private void validateProductData(String name, Number price, List<String> colors, List<String> sizes, Integer quant) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (price == null || price.doubleValue() <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }

        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("O produto precisa ter ao menos uma cor.");
        }

        if (sizes == null || sizes.isEmpty()) {
            throw new IllegalArgumentException("O produto precisa ter ao menos um tamanho.");
        }

        if (quant == null || quant < 1) {
            throw new IllegalArgumentException("A quantidade deve ser maior ou igual a 1.");
        }
    }
}
