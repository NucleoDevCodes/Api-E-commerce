package com.ecommerce.infra.exceptions;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long orderId) {
        super("Pagamento não encontrado para o pedido com id: " + orderId);
    }
}
