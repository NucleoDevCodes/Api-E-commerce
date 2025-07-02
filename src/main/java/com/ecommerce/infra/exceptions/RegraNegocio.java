package com.ecommerce.infra.exceptions;

public class RegraNegocio extends RuntimeException {
    public RegraNegocio(String message) {
        super(message);
    }
}
