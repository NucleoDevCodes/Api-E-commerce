package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.ProductsRecords.DataProducts;
import com.ecommerce.aplication.records.ProductsRecords.DataProductsResponse;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ServiceProductsWrite {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProductsWrite.class);

    public final ProductRepository repository;
    private final ServiceAsync serviceAsync;

    public ServiceProductsWrite(ProductRepository repository, ServiceAsync serviceAsync) {
        this.repository = repository;
        this.serviceAsync = serviceAsync;
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

        serviceAsync.updateRecommendations(saved);
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
        serviceAsync.updateRecommendations(saved);

        return toResponseDto(saved);
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

    public void save(ProductModel product) {
        repository.save(product);
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
