package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.ProductsRecords.DataProducts;
import com.ecommerce.aplication.records.ProductsRecords.DataProductsResponse;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ServiceProducts {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProducts.class);

    private final ProductRepository repository;

    public ServiceProducts(ProductRepository repository) {
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

    @Transactional
    public DataProductsResponse create(DataProducts data) {
        logger.info("Criando novo produto: {}", data.name());

        List<String> normalizedColors = normalizeList(data.colors());
        List<String> normalizedSizes = normalizeList(data.sizes());

        validateProductData(data.name(), data.price(), normalizedColors, normalizedSizes, data.quant());

        ProductModel product = new ProductModel(data);
        product.setColors(normalizedColors);
        product.setSizes(normalizedSizes);
        product.setQuant(data.quant());

        ProductModel saved = repository.save(product);

        logger.info("Produto criado com sucesso, ID: {}", saved.getId());
        return toResponseDto(saved);
    }

    @Transactional
    public DataProductsResponse update(Long id, DataProducts data) {
        logger.info("Atualizando produto ID: {}", id);

        List<String> normalizedColors = normalizeList(data.colors());
        List<String> normalizedSizes = normalizeList(data.sizes());

        validateProductData(data.name(), data.price(), normalizedColors, normalizedSizes, data.quant());

        ProductModel existing = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Produto ID {} não encontrado para atualização", id);
                    return new ResourceNotFoundException("Produto com ID " + id + " não encontrado.");
                });

        existing.setName(data.name());
        existing.setPrice(data.price());
        existing.setColors(normalizedColors);
        existing.setSizes(normalizedSizes);
        existing.setItem(data.item());
        existing.setType(data.type());
        existing.setQuant(data.quant());

        ProductModel saved = repository.save(existing);
        logger.info("Produto ID {} atualizado com sucesso", id);

        return toResponseDto(saved);
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

    @Transactional
    public void delete(Long id) {
        logger.info("Deletando produto ID: {}", id);

        ProductModel existing = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Produto ID {} não encontrado para exclusão", id);
                    return new ResourceNotFoundException("Produto com ID " + id + " não encontrado.");
                });

        repository.delete(existing);
        logger.info("Produto ID {} deletado com sucesso", id);
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

    private List<String> normalizeList(List<String> input) {
        if (input == null || input.isEmpty()) {
            logger.warn("Lista de cores ou tamanhos vazia ou nula");
            throw new BusinessRuleException("A lista não pode estar vazia.");
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
            logger.warn("Nome obrigatório não informado");
            throw new BusinessRuleException("O nome é obrigatório.");
        }

        if (price == null || price.doubleValue() <= 0) {
            logger.warn("Preço inválido informado: {}", price);
            throw new BusinessRuleException("O preço deve ser maior que zero.");
        }

        if (colors == null || colors.isEmpty()) {
            logger.warn("Lista de cores vazia");
            throw new BusinessRuleException("O produto precisa ter ao menos uma cor.");
        }

        if (sizes == null || sizes.isEmpty()) {
            logger.warn("Lista de tamanhos vazia");
            throw new BusinessRuleException("O produto precisa ter ao menos um tamanho.");
        }

        if (quant == null || quant < 1) {
            logger.warn("Quantidade inválida informada: {}", quant);
            throw new BusinessRuleException("A quantidade deve ser maior ou igual a 1.");
        }
    }
}
