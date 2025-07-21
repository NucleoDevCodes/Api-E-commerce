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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceCart {

    private final Logger logger= LoggerFactory.getLogger(ServiceCart.class);
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

    @Transactional
    public void addProductToCart(Long userId, DataCartItemRequest request) {
        logger.info("Usuário {} adicionando produto {} ao carrinho", userId, request.productId());
        var cart = cartRepository.findByUsersId(userId).orElseGet(() -> {
            logger.info("Carrinho não encontrado para o usuário {}. Criando novo carrinho.", userId);
            return createCartForUser(userId);
        });

        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> {
                    logger.warn("Produto {} não encontrado ao adicionar ao carrinho do usuário {}", request.productId(), userId);
                    return new ResourceNotFoundException("Produto não encontrado");
                });

        if (product.getQuant() < request.quantity()) {
            logger.warn("Estoque insuficiente para o produto {} ao adicionar no carrinho do usuário {}", product.getName(), userId);
            throw new StockUnavailableException("Estoque insuficiente para o produto: " + product.getName());
        }

        var item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> new CartItem());

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.quantity());

        cartItemRepository.save(item);
        logger.info("Produto {} adicionado ao carrinho do usuário {}", product.getName(), userId);
    }

    @Transactional
    public void removeProductFromCart(Long userId, Long productId) {
        logger.info("Usuário {} removendo produto {} do carrinho", userId, productId);
        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> {
                    logger.warn("Carrinho não encontrado para o usuário {}", userId);
                    return new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId);
                });

        var item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> {
                    logger.warn("Item do produto {} não encontrado no carrinho do usuário {}", productId, userId);
                    return new CartItemNotFoundException("Item do produto com ID: " + productId + " não encontrado no carrinho.");
                });

        cartItemRepository.delete(item);
        logger.info("Produto {} removido do carrinho do usuário {}", productId, userId);
    }

    public List<DataCartItemResponse> getCartItems(Long userId) {
        logger.info("Buscando itens do carrinho para o usuário {}", userId);
        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> {
                    logger.warn("Carrinho não encontrado para o usuário {}", userId);
                    return new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId);
                });

        return cart.getItems().stream()
                .map(item -> new DataCartItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity()))
                .collect(Collectors.toList());
    }

    public void clearCart(Long userId) {
        logger.info("Limpando carrinho do usuário {}", userId);
        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> {
                    logger.warn("Carrinho não encontrado para o usuário {}", userId);
                    return new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId);
                });

        cart.getItems().clear();
        cartRepository.save(cart);
        logger.info("Carrinho do usuário {} limpo com sucesso", userId);
    }

    @Transactional
    private CartModel createCartForUser(Long userId) {
        logger.info("Criando carrinho para o usuário {}", userId);
        var user = usersRepositroy.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Usuário {} não encontrado ao criar carrinho", userId);
                    return new UserNotFoundException(userId);
                });

        var cartData = new DataCart(user, new ArrayList<>());
        var cart = new CartModel(cartData);
        var savedCart = cartRepository.save(cart);
        logger.info("Carrinho criado para o usuário {} com ID {}", userId, savedCart.getId());
        return savedCart;
    }

    @Transactional
    public void finalizeCart(Long userId) {
        logger.info("Finalizando carrinho do usuário {}", userId);
        var cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> {
                    logger.warn("Carrinho não encontrado para finalizar para o usuário {}", userId);
                    return new CartNotFoundException("Carrinho não encontrado para o usuário com ID: " + userId);
                });

        for (CartItem item : cart.getItems()) {
            var product = item.getProduct();
            int remaining = product.getQuant() - item.getQuantity();

            if (remaining < 0) {
                logger.warn("Estoque insuficiente para o produto {} ao finalizar carrinho do usuário {}", product.getName(), userId);
                throw new StockUnavailableException("Estoque insuficiente para o produto: " + product.getName());
            }

            product.setQuant(remaining);
            productRepository.save(product);
            logger.debug("Estoque atualizado para o produto {}. Quantidade restante: {}", product.getName(), remaining);
        }

        cart.getItems().clear();
        cartRepository.save(cart);
        logger.info("Carrinho do usuário {} finalizado e limpo", userId);
    }
}