package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.OrderRecords.DataOrderItemResponse;
import com.ecommerce.aplication.records.OrderRecords.DataOrderResponse;
import com.ecommerce.infra.exceptions.CartEmptyException;
import com.ecommerce.infra.exceptions.OrderNotFoundException;
import com.ecommerce.infra.exceptions.StockUnavailableException;
import com.ecommerce.infra.exceptions.UserNotFoundException;
import com.ecommerce.model.cart.cartItem.CartItem;
import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.orders.OrderStatus;
import com.ecommerce.model.orders.ordersItems.OrderItem;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.*;
import com.ecommerce.model.users.Users;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceOrders {

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
        Users user = usersRepositroy.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new OrderNotFoundException(userId));

        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new CartEmptyException();
        }

        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            ProductModel product = item.getProduct();

            if (product.getQuant() < item.getQuantity()) {
                throw new StockUnavailableException(product.getName());
            }

            product.setQuant(product.getQuant() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            return orderItem;
        }).collect(Collectors.toList());

        OrderModel order = new OrderModel();
        order.setUsers(user);
        order.setStatus(OrderStatus.PENDENTE);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(orderItems);

        orderItems.forEach(item -> item.setOrder(order));

        ordersRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        List<DataOrderItemResponse> itemResponses = orderItems.stream().map(i ->
                new DataOrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getPrice()
                )
        ).toList();

        return new DataOrderResponse(order.getId(), order.getStatus(), order.getCreatedAt(), itemResponses);
    }

    public List<DataOrderResponse> listOrdersByUser(Long userId) {
        return ordersRepository.findByUsersId(userId).stream().map(order -> {
            List<DataOrderItemResponse> items = order.getItems().stream().map(i ->
                    new DataOrderItemResponse(
                            i.getProduct().getId(),
                            i.getProduct().getName(),
                            i.getQuantity(),
                            i.getPrice()
                    )
            ).toList();

            return new DataOrderResponse(order.getId(), order.getStatus(), order.getCreatedAt(), items);
        }).toList();
    }

}
