package com.ecommerce.infra.exceptions;

public class StockUnavailableException extends RuntimeException {
    public StockUnavailableException(String productName) {
        super("Estoque insuficiente para o produto: " + productName);
    }
}
