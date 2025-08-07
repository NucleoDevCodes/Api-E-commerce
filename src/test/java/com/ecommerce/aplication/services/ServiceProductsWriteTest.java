package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.ProductsRecords.DataProducts;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceProductsWriteTest {
    @Mock
    private ProductRepository repository;

    @Mock
    private ServiceAsync serviceAsync;

    @InjectMocks
    private ServiceProductsWrite service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private DataProducts createValidData() {
        return new DataProducts(
                "Tênis",
                BigDecimal.valueOf(150.0),
                "Descrição do tênis",
                CategoryItem.TÊNIS,
                CategoryType.CALÇADOS,
                10,
                List.of("M"),
                List.of("AZUL"),
                "https://cdn.imagens.com/tenis.jpg"
        );
    }

    private ProductModel createProduct(Long id, String name, List<String> sizes, List<String> colors, String imageUrl) {
        ProductModel p = new ProductModel();
        p.setId(id);
        p.setName(name);
        p.setPrice(BigDecimal.valueOf(150.0));
        p.setQuant(10);
        p.setItem(CategoryItem.TÊNIS);
        p.setType(CategoryType.CALÇADOS);
        p.setSizes(sizes);
        p.setColors(colors);
        p.setImageUrl(imageUrl);
        return p;
    }

    @Test
    void createProduct_Success() {
        DataProducts data = createValidData();

        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> {
            ProductModel p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        DataProductsResponse response = service.create(data);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(data.name(), response.name());
        assertEquals(data.imageUrl(), response.imageUrl());
        verify(serviceAsync).updateRecommendations(any());
    }

    @Test
    void createProduct_DuplicateThrows() {
        DataProducts data = createValidData();

        ProductModel existing = createProduct(1L, data.name(), data.sizes(), data.colors(), data.imageUrl());

        when(repository.findAll()).thenReturn(List.of(existing));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.create(data));
        assertEquals("Produto já cadastrado com estas especificações.", ex.getMessage());
    }

    @Test
    void createProduct_InvalidDataThrows() {
        DataProducts invalidData = new DataProducts(
                "",
                BigDecimal.valueOf(-10.0),
                "Descrição inválida",
                null,
                null,
                0,
                List.of(),
                List.of(),
                ""
        );

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.create(invalidData));
        assertTrue(ex.getMessage().contains("obrigatório") || ex.getMessage().contains("maior que zero") || ex.getMessage().contains("não pode estar vazia"));
    }

    @Test
    void updateProduct_Success() {
        Long id = 1L;
        DataProducts data = createValidData();
        ProductModel existing = createProduct(id, "OldName", List.of("M"), List.of("AZUL"), data.imageUrl());

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DataProductsResponse response = service.update(id, data);

        assertEquals(data.name(), response.name());
        assertEquals(data.imageUrl(), response.imageUrl());
        verify(serviceAsync).updateRecommendations(any());
    }

    @Test
    void updateProduct_NotFoundThrows() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.update(1L, createValidData()));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    void deleteProduct_Success() {
        ProductModel existing = createProduct(1L, "Nome", List.of("M"), List.of("AZUL"), "https://cdn.imagens.com/tenis.jpg");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }

    @Test
    void deleteProduct_NotFoundThrows() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    void normalizeList_EmptyOrNull_Throws() {
        BusinessRuleException ex1 = assertThrows(BusinessRuleException.class, () -> service.create(
                new DataProducts("Test", BigDecimal.valueOf(10.0), "Desc", CategoryItem.CAMISETA, CategoryType.MASCULINO, 1, null, List.of("M"), "url")
        ));
        BusinessRuleException ex2 = assertThrows(BusinessRuleException.class, () -> service.create(
                new DataProducts("Test", BigDecimal.valueOf(10.0), "Desc", CategoryItem.CAMISETA, CategoryType.MASCULINO, 1, List.of(), List.of("M"), "url")
        ));

        assertTrue(ex1.getMessage().contains("não pode estar vazia"));
        assertTrue(ex2.getMessage().contains("não pode estar vazia"));
    }
}