package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.ProductsRecords.DataProductsResponse;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ServiceProductRead {
    private final ProductRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(ServiceProductRead.class);

    public ServiceProductRead(ProductRepository repository) {
        this.repository = repository;
    }

    private DataProductsResponse toResponseDto(ProductModel product) {
        return new DataProductsResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuant(),
                product.getItem(),
                product.getType(),
                product.getSizes(),
                product.getColors()
        );
    }

    public ProductModel findProductEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    public DataProductsResponse findById(Long id) {
        logger.debug("Buscando produto por ID: {}", id);

        ProductModel model = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Produto ID {} não encontrado", id);
                    return new ResourceNotFoundException("Produto com ID " + id + " não encontrado.");
                });

        return toResponseDto(model);
    }

    public Page<DataProductsResponse> findAll(int page) {
        logger.debug("Buscando todos os produtos, página {}", page);
        return repository.findAll(PageRequest.of(page, 5)).map(this::toResponseDto);
    }

    public Page<DataProductsResponse> findByItem(CategoryItem item, int page) {
        logger.debug("Buscando produtos por item {} na página {}", item, page);
        return repository.findByItem(item, PageRequest.of(page, 5)).map(this::toResponseDto);
    }

    public Page<DataProductsResponse> findByType(CategoryType type, int page) {
        logger.debug("Buscando produtos por tipo {} na página {}", type, page);
        return repository.findByType(type, PageRequest.of(page, 5)).map(this::toResponseDto);
    }

    public Page<DataProductsResponse> findByItemAndType(CategoryItem item, CategoryType type, int page) {
        logger.debug("Buscando produtos por item {} e tipo {} na página {}", item, type, page);
        return repository.findByItemAndType(item, type, PageRequest.of(page, 5)).map(this::toResponseDto);
    }

    public Page<DataProductsResponse> findByNameContaining(String name, int page) {
        logger.debug("Buscando produtos com nome contendo: '{}', página {}", name, page);

        if (name == null || name.isBlank()) {
            logger.warn("Termo de busca vazio");
            throw new BusinessRuleException("Termo de busca não pode ser vazio.");
        }

        Pageable pageable = PageRequest.of(page, 5);
        return repository.findByNameContainingIgnoreCase(name.trim(), pageable).map(this::toResponseDto);
    }

    public Page<DataProductsResponse> findBySize(String size, int page) {
        String normalizedSize = normalize(size);
        logger.debug("Buscando produtos por tamanho: '{}', página {}", normalizedSize, page);

        Pageable pageable = PageRequest.of(page, 5);
        return repository.findBySizesContainingIgnoreCase(normalizedSize, pageable).map(this::toResponseDto);
    }

    public Page<DataProductsResponse> findByColor(String color, int page) {
        String normalizedColor = normalize(color);
        logger.debug("Buscando produtos por cor: '{}', página {}", normalizedColor, page);

        Pageable pageable = PageRequest.of(page, 5);
        return repository.findByColorsContainingIgnoreCase(normalizedColor, pageable).map(this::toResponseDto);
    }

    public Page<DataProductsResponse> findAllOrderByPrice(String priceSort, int page) {
        logger.debug("Buscando produtos ordenados por preço '{}', página {}", priceSort, page);

        Sort sort = "desc".equalsIgnoreCase(priceSort)
                ? Sort.by("price").descending()
                : Sort.by("price").ascending();

        Pageable pageable = PageRequest.of(page, 5, sort);
        return repository.findAll(pageable).map(this::toResponseDto);
    }

    public boolean existsByNameAndColorAndSize(String name, String color, String size) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Nome obrigatório não informado");
            throw new BusinessRuleException("Nome é obrigatório.");
        }

        if (color == null || color.trim().isEmpty()) {
            logger.warn("Cor obrigatória não informada");
            throw new BusinessRuleException("Cor é obrigatória.");
        }

        if (size == null || size.trim().isEmpty()) {
            logger.warn("Tamanho obrigatório não informado");
            throw new BusinessRuleException("Tamanho é obrigatório.");
        }

        return repository.existsByNameAndColorAndSize(
                name.trim(),
                color.trim().toUpperCase(),
                size.trim().toUpperCase()
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

}
