package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.OrderRecords.DataOrderItemResponse;
import com.ecommerce.aplication.records.OrderRecords.DataOrderResponse;
import com.ecommerce.infra.exceptions.CartEmptyException;
import com.ecommerce.infra.exceptions.OrderNotFoundException;
import com.ecommerce.infra.exceptions.StockUnavailableException;
import com.ecommerce.infra.exceptions.UserNotFoundException;
import com.ecommerce.model.cart.CartModel;
import com.ecommerce.model.cart.cartItem.CartItem;
import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.orders.OrderStatus;
import com.ecommerce.model.orders.ordersItems.OrderItem;
import com.ecommerce.model.repositorys.*;
import com.ecommerce.model.users.Users;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceOrders {

    private final Logger logger= LoggerFactory.getLogger(ServiceOrders.class);
    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private  final UsersRepositroy usersRepositroy;


    public ServiceOrders(OrdersRepository ordersRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UsersRepositroy usersRepositroy) {
        this.ordersRepository = ordersRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.usersRepositroy = usersRepositroy;
    }

    @Transactional
    public DataOrderResponse checkout(Long userId) {
        logger.info("Iniciando checkout para o usuário {}", userId);

        var user = findUserById(userId);
        var cart = findCartByUserId(userId);

        var cartItems = cartItemRepository.findAllByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            logger.warn("Carrinho vazio para o usuário {}", userId);
            throw new CartEmptyException();
        }

        var orderItems = buildOrderItems(cartItems);
        var order = buildOrder(user, orderItems);

        ordersRepository.save(order);
        cartItemRepository.deleteAll(cartItems);

        logger.info("Checkout finalizado para o usuário {} com pedido ID {}", userId, order.getId());
        return buildOrderResponse(order, orderItems);
    }

    private Users findUserById(Long id) {
        return usersRepositroy.findById(id).orElseThrow(() -> {
            logger.warn("Usuário não encontrado com ID {}", id);
            return new UserNotFoundException(id);
        });
    }

    private CartModel findCartByUserId(Long userId) {
        return cartRepository.findByUsersId(userId).orElseThrow(() -> {
            logger.warn("Carrinho não encontrado para o usuário {}", userId);
            return new OrderNotFoundException(userId);
        });
    }

    private List<OrderItem> buildOrderItems(List<CartItem> cartItems) {
        return cartItems.stream().map(item -> {
            var product = item.getProduct();
            if (product.getQuant() < item.getQuantity()) {
                logger.warn("Estoque insuficiente para o produto {} durante checkout", product.getName());
                throw new StockUnavailableException(product.getName());
            }
            product.setQuant(product.getQuant() - item.getQuantity());
            productRepository.save(product);

            var orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            return orderItem;
        }).toList();
    }

    private OrderModel buildOrder(Users user, List<OrderItem> items) {
        var order = new OrderModel();
        order.setUsers(user);
        order.setStatus(OrderStatus.PENDENTE);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(items);
        items.forEach(i -> i.setOrder(order));
        logger.debug("Pedido criado para o usuário {} com {} itens", user.getId(), items.size());
        return order;
    }

    private DataOrderResponse buildOrderResponse(OrderModel order, List<OrderItem> items) {
        var itemResponses = items.stream().map(i -> new DataOrderItemResponse(
                i.getProduct().getId(),
                i.getProduct().getName(),
                i.getQuantity(),
                i.getPrice()
        )).toList();

        return new DataOrderResponse(order.getId(), order.getStatus(), order.getCreatedAt(), itemResponses);
    }

}