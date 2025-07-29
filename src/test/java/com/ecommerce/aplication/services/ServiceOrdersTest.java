package com.ecommerce.aplication.services;

import static org.junit.jupiter.api.Assertions.*;
import com.ecommerce.aplication.records.OrderRecords.DataOrderResponse;
import com.ecommerce.infra.exceptions.*;
import com.ecommerce.model.cart.CartModel;
import com.ecommerce.model.cart.cartItem.CartItem;
import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.orders.OrderStatus;
import com.ecommerce.model.orders.ordersItems.OrderItem;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.*;
import com.ecommerce.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class ServiceOrdersTest {
    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ServiceAsync serviceAsync;

    @Mock
    private UsersRepositroy usersRepositroy;

    @InjectMocks
    private ServiceOrders serviceOrders;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Users createUser(Long id) {
        Users user = new Users();
        user.setId(id);
        user.setName("User " + id);
        return user;
    }

    private ProductModel createProduct(Long id, String name, int quant) {
        ProductModel product = new ProductModel();
        product.setId(id);
        product.setName(name);
        product.setQuant(quant);
        product.setPrice(BigDecimal.valueOf(10.0));
        return product;
    }

    private CartModel createCart(Long cartId, Users user, List<CartItem> items) {
        CartModel cart = new CartModel();
        cart.setId(cartId);
        cart.setUsers(user);
        cart.setItems(items);
        return cart;
    }

    private CartItem createCartItem(Long id, ProductModel product, int quantity, String color, String size) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setColor(color);
        item.setSize(size);
        return item;
    }

    private OrderModel createOrder(Long id, Users user, List<OrderItem> items) {
        OrderModel order = new OrderModel();
        order.setId(id);
        order.setUsers(user);
        order.setStatus(OrderStatus.PENDENTE);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(items);
        return order;
    }

    @Test
    void checkout_successful() {
        Long userId = 1L;
        Users user = createUser(userId);

        ProductModel product = createProduct(1L, "Produto", 10);
        CartItem cartItem = createCartItem(1L, product, 2, "RED", "M");
        CartModel cart = createCart(1L, user, List.of(cartItem));

        when(usersRepositroy.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ordersRepository.save(any(OrderModel.class))).thenAnswer(i -> {
            OrderModel o = i.getArgument(0);
            o.setId(100L);
            return o;
        });

        DataOrderResponse response = serviceOrders.checkout(userId);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDENTE, response.status());
        assertEquals(1, response.items().size());
        assertEquals("Produto", response.items().get(0).productName());

        verify(ordersRepository).save(any(OrderModel.class));
        verify(cartItemRepository).deleteAll(any());
        verify(serviceAsync).sendConfirmationEmail(any());
        verify(serviceAsync).updateRecommendationsForOrder(any());
    }


    @Test
    void checkout_userNotFound_throwsException() {
        Long userId = 1L;

        when(usersRepositroy.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> serviceOrders.checkout(userId));
        assertTrue(ex.getMessage().contains(userId.toString()));

        verify(cartRepository, never()).findByUsersId(any());
    }

    @Test
    void checkout_insufficientStock_throwsException() {
        Long userId = 1L;
        Users user = createUser(userId);

        ProductModel product = createProduct(1L, "Produto", 1);
        CartItem cartItem = createCartItem(1L, product, 5, "RED", "M");
        CartModel cart = createCart(1L, user, List.of(cartItem));

        when(usersRepositroy.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartId(cart.getId())).thenReturn(List.of(cartItem));

        StockUnavailableException ex = assertThrows(StockUnavailableException.class, () -> serviceOrders.checkout(userId));
        assertTrue(ex.getMessage().contains(product.getName()));

        verify(ordersRepository, never()).save(any());
    }

    @Test
    void checkout_duplicateItems_throwsException() {
        Long userId = 1L;
        Users user = createUser(userId);

        ProductModel product = createProduct(1L, "Produto", 10);
        CartItem item1 = createCartItem(1L, product, 1, "RED", "M");
        CartItem item2 = createCartItem(2L, product, 1, "red", "m");

        CartModel cart = createCart(1L, user, List.of(item1, item2));

        when(usersRepositroy.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartId(cart.getId())).thenReturn(List.of(item1, item2));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serviceOrders.checkout(userId));
        assertTrue(ex.getMessage().contains("duplicados"));

        verify(ordersRepository, never()).save(any());
    }

    @Test
    void checkout_emptyCart_throwsException() {
        Long userId = 1L;
        Users user = createUser(userId);
        CartModel cart = createCart(1L, user, List.of());

        when(usersRepositroy.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUsersId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartId(cart.getId())).thenReturn(List.of());

        CartEmptyException ex = assertThrows(CartEmptyException.class, () -> serviceOrders.checkout(userId));
        assertEquals("O carrinho está vazio. Não é possível finalizar o pedido.", ex.getMessage());

        verify(ordersRepository, never()).save(any());
    }

    @Test
    void listOrdersByUser_returnsOrders() {
        Long userId = 1L;
        Users user = createUser(userId);

        ProductModel product = createProduct(1L, "Produto", 10);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(BigDecimal.valueOf(10.0));
        orderItem.setColor("RED");
        orderItem.setSize("M");

        OrderModel order = createOrder(100L, user, List.of(orderItem));

        when(ordersRepository.findByUsersId(userId)).thenReturn(List.of(order));

        var responses = serviceOrders.listOrdersByUser(userId);

        assertEquals(1, responses.size());
        assertEquals(order.getId(), responses.get(0).orderId());
        assertEquals(1, responses.get(0).items().size());
        assertEquals("Produto", responses.get(0).items().get(0).productName());
    }



}