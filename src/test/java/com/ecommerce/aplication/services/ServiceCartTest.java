package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.CartRecords.DataCartItemRequest;
import com.ecommerce.aplication.records.CartRecords.DataCartItemResponse;
import com.ecommerce.infra.exceptions.*;
import com.ecommerce.model.cart.CartModel;
import com.ecommerce.model.cart.cartItem.CartItem;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.CartItemRepository;
import com.ecommerce.model.repositorys.CartRepository;
import com.ecommerce.model.repositorys.ProductRepository;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
;

class ServiceCartTest {
    private CartItemRepository cartItemRepository;
    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private UsersRepositroy usersRepositroy;
    private ServiceCart serviceCart;

    @BeforeEach
    void setUp() {
        cartItemRepository = mock(CartItemRepository.class);
        cartRepository = mock(CartRepository.class);
        productRepository = mock(ProductRepository.class);
        usersRepositroy = mock(UsersRepositroy.class);
        serviceCart = new ServiceCart(cartItemRepository, cartRepository, productRepository, usersRepositroy);
    }

    private Users createUser(Long id) {
        Users user = new Users();
        user.setId(id);
        user.setName("User " + id);
        return user;
    }

    private ProductModel createProduct(Long id, String name, int quantity) {
        ProductModel product = new ProductModel();
        product.setId(id);
        product.setName(name);
        product.setQuant(quantity);
        return product;
    }

    private CartModel createCart(Long id, Users user, List<CartItem> items) {
        CartModel cart = new CartModel();
        cart.setId(id);
        cart.setUsers(user);
        cart.setItems(new ArrayList<>(items));
        return cart;
    }

    private CartItem createCartItem(Long productId, String color, String size, int quantity, ProductModel product, CartModel cart) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setCart(cart);
        item.setColor(color);
        item.setSize(size);
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void addProductToCart_createNewCart_success() {
        Long userId = 1L;
        DataCartItemRequest req = new DataCartItemRequest(10L, 2, "RED", "M");

        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.empty());
        Users user = createUser(userId);
        when(usersRepositroy.findById(userId)).thenReturn(Optional.of(user));
        ProductModel product = createProduct(req.productId(), "Prod X", 10);
        when(productRepository.findById(req.productId())).thenReturn(Optional.of(product));
        CartModel newCart = createCart(100L, user, new ArrayList<>());
        when(cartRepository.save(any(CartModel.class))).thenReturn(newCart);

        serviceCart.addProductToCart(userId, req);

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_existingCartWithProduct_shouldThrow() {
        Long userId = 1L;
        DataCartItemRequest req = new DataCartItemRequest(10L, 2, "RED", "M");

        Users user = createUser(userId);
        ProductModel product = createProduct(req.productId(), "Prod X", 10);

        CartModel cart = createCart(100L, user, new ArrayList<>());
        CartItem existingItem = createCartItem(product.getId(), "RED", "M", 1, product, cart);
        cart.getItems().add(existingItem);

        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(req.productId())).thenReturn(Optional.of(product));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            serviceCart.addProductToCart(userId, req);
        });

        assertEquals("Este produto já está no carrinho com mesma cor e tamanho.", ex.getMessage());
    }

    @Test
    void removeProductFromCart_success() {
        Long userId = 1L;
        Long productId = 10L;

        Users user = createUser(userId);
        ProductModel product = createProduct(productId, "Prod X", 10);
        CartModel cart = createCart(100L, user, new ArrayList<>());
        CartItem item = createCartItem(productId, "RED", "M", 2, product, cart);
        cart.getItems().add(item);

        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)).thenReturn(Optional.of(item));

        serviceCart.removeProductFromCart(userId, productId);

        verify(cartItemRepository, times(1)).delete(item);
    }

    @Test
    void finalizeCart_success() {
        Long userId = 1L;
        Users user = createUser(userId);
        ProductModel product = createProduct(10L, "Prod X", 5);
        CartModel cart = createCart(100L, user, new ArrayList<>());

        CartItem item = createCartItem(10L, "RED", "M", 3, product, cart);
        cart.getItems().add(item);

        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.save(any(ProductModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(CartModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serviceCart.finalizeCart(userId);

        assertEquals(2, product.getQuant());
        assertTrue(cart.getItems().isEmpty());

        verify(productRepository, times(1)).save(product);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void finalizeCart_insufficientStock_shouldThrow() {
        Long userId = 1L;
        Users user = createUser(userId);
        ProductModel product = createProduct(10L, "Prod X", 2);
        CartModel cart = createCart(100L, user, new ArrayList<>());

        CartItem item = createCartItem(10L, "RED", "M", 3, product, cart);
        cart.getItems().add(item);

        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));

        StockUnavailableException ex = assertThrows(StockUnavailableException.class, () -> {
            serviceCart.finalizeCart(userId);
        });

        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
    }

    @Test
    void clearCart_success() {
        Long userId = 1L;
        Users user = createUser(userId);
        CartModel cart = createCart(100L, user, new ArrayList<>());

        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));

        serviceCart.clearCart(userId);

        verify(cartItemRepository, times(1)).deleteAllByCartId(cart.getId());
        verify(cartRepository, times(1)).save(cart);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void getCartItems_success() {
        Long userId = 1L;
        Users user = createUser(userId);
        ProductModel product = createProduct(10L, "Prod X", 10);
        CartModel cart = createCart(100L, user, new ArrayList<>());

        CartItem item = createCartItem(10L, "RED", "M", 3, product, cart);
        cart.getItems().add(item);

        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));

        List<DataCartItemResponse> responses = serviceCart.getCartItems(userId);

        assertEquals(1, responses.size());
        assertEquals(product.getId(), responses.get(0).productId());
        assertEquals(product.getName(), responses.get(0).productName());
        assertEquals(3, responses.get(0).quantity());
        assertEquals("RED", responses.get(0).color());
        assertEquals("M", responses.get(0).size());
    }
}