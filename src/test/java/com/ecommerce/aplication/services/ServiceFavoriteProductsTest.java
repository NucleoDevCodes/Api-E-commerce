package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.FavoriteProductRecords.DataFavoriteProductRequest;
import com.ecommerce.aplication.records.FavoriteProductRecords.DataFavoriteProductResponse;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.favorite.FavoriteProducts;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.FavoriteProductsRepository;
import com.ecommerce.model.repositorys.ProductRepository;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceFavoriteProductsTest {
    @Mock
    private FavoriteProductsRepository favoRepo;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private UsersRepositroy userRepo;

    @InjectMocks
    private ServiceFavoriteProducts serviceFavoriteProducts;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Users createUser(Long id) {
        Users user = new Users();
        user.setId(id);
        user.setName("User " + id);
        return user;
    }

    private ProductModel createProduct(Long id, String name, CategoryItem item, CategoryType type) {
        ProductModel p = new ProductModel();
        p.setId(id);
        p.setName(name);
        p.setItem(item);
        p.setType(type);
        return p;
    }

    private FavoriteProducts createFavorite(UUID id, Users user, ProductModel product) {
        return new FavoriteProducts(id, user, product, null);
    }

    @Test
    void add_success() {
        Long userId = 1L;
        Long productId = 2L;
        DataFavoriteProductRequest request = new DataFavoriteProductRequest(productId);

        when(favoRepo.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        Users user = createUser(userId);
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        ProductModel product = createProduct(productId, "Produto X", CategoryItem.CAMISETA, CategoryType.MASCULINO);
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));

        serviceFavoriteProducts.add(userId, request);

        verify(favoRepo).save(any(FavoriteProducts.class));
    }

    @Test
    void add_duplicateFavorite_throws() {
        Long userId = 1L;
        Long productId = 2L;
        DataFavoriteProductRequest request = new DataFavoriteProductRequest(productId);

        when(favoRepo.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(new FavoriteProducts()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serviceFavoriteProducts.add(userId, request));
        assertEquals("Produto já está nos favoritos.", ex.getMessage());

        verify(favoRepo, never()).save(any());
    }

    @Test
    void add_userNotFound_throws() {
        Long userId = 1L;
        Long productId = 2L;
        DataFavoriteProductRequest request = new DataFavoriteProductRequest(productId);

        when(favoRepo.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> serviceFavoriteProducts.add(userId, request));
        assertTrue(ex.getMessage().contains("Usuário não encontrado"));

        verify(favoRepo, never()).save(any());
    }

    @Test
    void add_productNotFound_throws() {
        Long userId = 1L;
        Long productId = 2L;
        DataFavoriteProductRequest request = new DataFavoriteProductRequest(productId);

        when(favoRepo.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        when(userRepo.findById(userId)).thenReturn(Optional.of(createUser(userId)));
        when(productRepo.findById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> serviceFavoriteProducts.add(userId, request));
        assertTrue(ex.getMessage().contains("Produto não encontrado"));

        verify(favoRepo, never()).save(any());
    }

    @Test
    void remove_success() {
        Long userId = 1L;
        Long productId = 2L;

        FavoriteProducts favorite = createFavorite(UUID.randomUUID(), createUser(userId), createProduct(productId, "P", CategoryItem.CAMISETA, CategoryType.MASCULINO));
        when(favoRepo.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(favorite));

        serviceFavoriteProducts.remove(userId, productId);

        verify(favoRepo).delete(favorite);
    }

    @Test
    void remove_notFound_throws() {
        Long userId = 1L;
        Long productId = 2L;

        when(favoRepo.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> serviceFavoriteProducts.remove(userId, productId));
        assertTrue(ex.getMessage().contains("Produto não está nos favoritos"));

        verify(favoRepo, never()).delete(any());
    }

    @Test
    void list_success() {
        Long userId = 1L;
        FavoriteProducts fav1 = createFavorite(UUID.randomUUID(), createUser(userId), createProduct(1L, "Prod1", CategoryItem.CAMISETA, CategoryType.MASCULINO));
        FavoriteProducts fav2 = createFavorite(UUID.randomUUID(), createUser(userId), createProduct(2L, "Prod2", CategoryItem.CAMISETA, CategoryType.FEMININO));

        when(favoRepo.findByUserId(userId)).thenReturn(List.of(fav1, fav2));

        List<DataFavoriteProductResponse> list = serviceFavoriteProducts.list(userId);

        assertEquals(2, list.size());
        assertEquals("Prod1", list.get(0).productName());
        assertEquals("Prod2", list.get(1).productName());
    }

    @Test
    void recommend_success() {
        Long userId = 1L;
        Users user = createUser(userId);
        ProductModel favProd = createProduct(1L, "ProdFav", CategoryItem.CAMISETA, CategoryType.MASCULINO);
        FavoriteProducts fav = createFavorite(UUID.randomUUID(), user, favProd);

        when(favoRepo.findByUserId(userId)).thenReturn(List.of(fav));

        ProductModel recProd = createProduct(2L, "ProdRec", CategoryItem.CAMISETA, CategoryType.MASCULINO);
        when(productRepo.findByItemInAndTypeInAndIdNotIn(
                List.of(CategoryItem.CAMISETA), List.of(CategoryType.MASCULINO), List.of(1L)))
                .thenReturn(List.of(recProd));

        List<DataFavoriteProductResponse> recs = serviceFavoriteProducts.recommend(userId);

        assertEquals(1, recs.size());
        assertEquals("ProdRec", recs.get(0).productName());
    }

    @Test
    void recommend_noFavorites_throws() {
        Long userId = 1L;
        when(favoRepo.findByUserId(userId)).thenReturn(List.of());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serviceFavoriteProducts.recommend(userId));
        assertEquals("Você ainda não possui favoritos para gerar recomendações.", ex.getMessage());
    }
}