package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.ProductsRecords.DataProductsResponse;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ServiceProductReadTest {
    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ServiceProductRead service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private ProductModel createProduct(Long id, String name) {
        ProductModel p = new ProductModel();
        p.setId(id);
        p.setName(name);
        p.setPrice(BigDecimal.valueOf(100.0));
        p.setQuant(10);
        p.setItem(CategoryItem.TÊNIS);
        p.setType(CategoryType.CALÇADOS);
        p.setSizes(List.of("M", "G"));
        p.setColors(List.of("AZUL", "PRETO"));
        return p;
    }

    @Test
    void findById_Success() {
        ProductModel product = createProduct(1L, "Tênis Azul");
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        DataProductsResponse response = service.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Tênis Azul", response.name());
    }

    @Test
    void findById_NotFoundThrows() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    void findAll_ReturnsPage() {
        ProductModel p1 = createProduct(1L, "Produto1");
        ProductModel p2 = createProduct(2L, "Produto2");

        Page<ProductModel> page = new PageImpl<>(List.of(p1, p2));
        when(repository.findAll(PageRequest.of(0, 5))).thenReturn(page);

        var result = service.findAll(0);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findByItem_Success() {
        ProductModel p = createProduct(1L, "Produto1");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findByItem(CategoryItem.TÊNIS, PageRequest.of(0,5))).thenReturn(page);

        var result = service.findByItem(CategoryItem.TÊNIS, 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByType_Success() {
        ProductModel p = createProduct(1L, "Produto1");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findByType(CategoryType.CALÇADOS, PageRequest.of(0,5))).thenReturn(page);

        var result = service.findByType(CategoryType.CALÇADOS, 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByItemAndType_Success() {
        ProductModel p = createProduct(1L, "Produto1");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findByItemAndType(CategoryItem.TÊNIS, CategoryType.CALÇADOS, PageRequest.of(0,5))).thenReturn(page);

        var result = service.findByItemAndType(CategoryItem.TÊNIS, CategoryType.CALÇADOS, 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByNameContaining_Success() {
        ProductModel p = createProduct(1L, "Tênis Azul");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findByNameContainingIgnoreCase("Tênis", PageRequest.of(0,5))).thenReturn(page);

        var result = service.findByNameContaining("Tênis", 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByNameContaining_EmptyNameThrows() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.findByNameContaining(" ", 0));
        assertTrue(ex.getMessage().contains("não pode ser vazio"));
    }

    @Test
    void findBySize_Success() {
        ProductModel p = createProduct(1L, "Produto1");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findBySizesContainingIgnoreCase("M", PageRequest.of(0,5))).thenReturn(page);

        var result = service.findBySize("M", 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByColor_Success() {
        ProductModel p = createProduct(1L, "Produto1");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findByColorsContainingIgnoreCase("AZUL", PageRequest.of(0,5))).thenReturn(page);

        var result = service.findByColor("azul", 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAllOrderByPrice_Ascending() {
        ProductModel p = createProduct(1L, "Produto1");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findAll(PageRequest.of(0, 5, Sort.by("price").ascending()))).thenReturn(page);

        var result = service.findAllOrderByPrice("asc", 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAllOrderByPrice_Descending() {
        ProductModel p = createProduct(1L, "Produto1");
        Page<ProductModel> page = new PageImpl<>(List.of(p));
        when(repository.findAll(PageRequest.of(0, 5, Sort.by("price").descending()))).thenReturn(page);

        var result = service.findAllOrderByPrice("desc", 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void existsByNameAndColorAndSize_Success() {
        when(repository.existsByNameAndColorAndSize("Tênis", "AZUL", "M")).thenReturn(true);

        boolean exists = service.existsByNameAndColorAndSize("Tênis", "azul", "m");
        assertTrue(exists);
    }

    @Test
    void existsByNameAndColorAndSize_MissingNameThrows() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.existsByNameAndColorAndSize(null, "AZUL", "M"));
        assertTrue(ex.getMessage().contains("Nome é obrigatório"));
    }

    @Test
    void existsByNameAndColorAndSize_MissingColorThrows() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.existsByNameAndColorAndSize("Tênis", null, "M"));
        assertTrue(ex.getMessage().contains("Cor é obrigatória"));
    }

    @Test
    void existsByNameAndColorAndSize_MissingSizeThrows() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.existsByNameAndColorAndSize("Tênis", "AZUL", null));
        assertTrue(ex.getMessage().contains("Tamanho é obrigatório"));
    }
}