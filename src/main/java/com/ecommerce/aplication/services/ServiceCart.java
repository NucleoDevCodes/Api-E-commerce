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

        CartModel cart = cartRepository.findByUsersId(userId).orElseGet(() -> {
            logger.info("Carrinho não encontrado para o usuário {}. Criando novo carrinho.", userId);
            return createCartForUser(userId);
        });

        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> {
                    logger.warn("Produto {} não encontrado", request.productId());
                    return new ResourceNotFoundException("Produto não encontrado");
                });

        if (product.getQuant() < request.quantity()) {
            logger.warn("Estoque insuficiente para o produto {}", product.getName());
            throw new StockUnavailableException("Estoque insuficiente para o produto: " + product.getName());
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(CartItem::new);

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.quantity());
        item.setColor(request.color());
        item.setSize(request.size());

        cartItemRepository.save(item);

        logger.info("Produto {} adicionado ao carrinho com cor '{}' e tamanho '{}'", product.getName(), request.color(), request.size());
    }

    @Transactional
    public void removeProductFromCart(Long userId, Long productId) {
        logger.info("Usuário {} removendo produto {} do carrinho", userId, productId);

        CartModel cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("Item não encontrado no carrinho"));

        cartItemRepository.delete(item);

        logger.info("Produto {} removido do carrinho", productId);
    }

    @Transactional(readOnly = true)
    public List<DataCartItemResponse> getCartItems(Long userId) {
        logger.info("Buscando itens do carrinho para o usuário {}", userId);

        CartModel cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado"));

        return cart.getItems().stream()
                .map(item -> new DataCartItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getColor(),
                        item.getSize()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearCart(Long userId) {
        logger.info("Limpando carrinho do usuário {}", userId);

        CartModel cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado"));

        cart.getItems().clear();
        cartItemRepository.deleteAllByCartId(cart.getId());

        cartRepository.save(cart);
        logger.info("Carrinho limpo com sucesso");
    }

    @Transactional
    private CartModel createCartForUser(Long userId) {
        logger.info("Criando carrinho para o usuário {}", userId);

        var user = usersRepositroy.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        var cartData = new DataCart(user, new ArrayList<>());
        var cart = new CartModel(cartData);

        return cartRepository.save(cart);
    }

    @Transactional
    public void finalizeCart(Long userId) {
        logger.info("Finalizando carrinho do usuário {}", userId);

        CartModel cart = cartRepository.findByUsersId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrinho não encontrado"));

        for (CartItem item : cart.getItems()) {
            var product = item.getProduct();
            int remaining = product.getQuant() - item.getQuantity();

            if (remaining < 0) {
                logger.warn("Estoque insuficiente para o produto {}", product.getName());
                throw new StockUnavailableException("Estoque insuficiente para o produto: " + product.getName());
            }

            product.setQuant(remaining);
            productRepository.save(product);
        }

        cart.getItems().clear();
        cartRepository.save(cart);
        logger.info("Carrinho finalizado");
    }
    }
