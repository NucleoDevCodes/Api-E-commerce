package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.CartRecords.DataCart;
import com.ecommerce.aplication.records.CartRecords.DataCartItemRequest;
import com.ecommerce.aplication.records.CartRecords.DataCartItemResponse;
import com.ecommerce.infra.exceptions.*;
import com.ecommerce.model.cart.CartModel;
import com.ecommerce.model.cart.cartItem.CartItem;
import com.ecommerce.model.repositorys.CartItemRepository;
import com.ecommerce.model.repositorys.CartRepository;
import com.ecommerce.model.repositorys.ProductRepository;
import com.ecommerce.model.repositorys.UsersRepositroy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceCart {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UsersRepositroy usersRepositroy;

    public ServiceCart(CartItemRepository cartItemRepository, CartRepository cartRepository, ProductRepository productRepository, UsersRepositroy usersRepositroy) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.usersRepositroy = usersRepositroy;
    }

    public void addItemToCart(Long userId, DataCartItemRequest request) {
        var cart = cartRepository.findByUsersId(userId).orElseGet(() -> createCartForUser(userId));

        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (product.getQuant() < request.quantity()) {
            throw new StockUnavailableException("Estoque insuficiente para o produto: " + product.getName());
        }

        var item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> new CartItem());

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.quantity());

        cartItemRepository.save(item);
    }



    public void removeItemCart(Long userId, Long productId) {
        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId));

        var item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("Item do produto com ID: " + productId + " não encontrado no carrinho."));

        cartItemRepository.delete(item);
    }

    public List<DataCartItemResponse> getCartItems(Long userId) {
        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId));

        return cart.getItems().stream()
                .map(item -> new DataCartItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity()))
                .collect(Collectors.toList());
    }

    public void clearCart(Long userId) {
        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId));

        cart.getItems().clear();
        cartRepository.save(cart);
    }


    private CartModel createCartForUser(Long userId) {
        var user = usersRepositroy.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        var cartData = new DataCart(user, new ArrayList<>());
        var cart = new CartModel(cartData);
        return cartRepository.save(cart);
    }

    private  void finalizeCart(Long userId ){
     var cart=cartRepository.findByUsersId(userId).orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId));

     for(CartItem item: cart.getItems()){
         var product= item.getProduct();
         int remaining=product.getQuant() - item.getQuantity();

        if (remaining < 0) {
            throw new StockUnavailableException("Estoque insuficiente para o produto: " + product.getName());
        }

        product.setQuant(remaining);
        productRepository.save(product);
    }
    }
}
