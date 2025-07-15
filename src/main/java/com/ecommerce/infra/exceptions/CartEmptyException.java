package com.ecommerce.infra.exceptions;

public class CartEmptyException extends RuntimeException {
    public CartEmptyException() {
        super("O carrinho está vazio. Não é possível finalizar o pedido.");
    }
}
