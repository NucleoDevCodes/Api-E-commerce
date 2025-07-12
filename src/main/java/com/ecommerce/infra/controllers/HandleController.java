package com.ecommerce.infra.controllers;

import com.ecommerce.infra.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class HandleController {
    private static final Logger logger = LoggerFactory.getLogger(HandleController.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<DataErroResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("Recurso não encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<DataErroResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        logger.warn("Regra de negócio violada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<DataErroResponse> handleUnauthorized(UnauthorizedActionException ex, HttpServletRequest request) {
        logger.warn("Ação não autorizada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DataErroResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Acesso negado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Acesso negado.", request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<DataErroResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Argumento inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<DataErroResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        logger.warn("Estado inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Estado inválido: " + ex.getMessage(), request.getRequestURI());
    }
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<DataErroResponse> handleCartNotFound(CartNotFoundException ex, HttpServletRequest request) {
        logger.warn("Carrinho não encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<DataErroResponse> handleCartItemNotFound(CartItemNotFoundException ex, HttpServletRequest request) {
        logger.warn("Item do carrinho não encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("Erro de validação: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        var errorResponse = new DataErroResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos campos",
                errors.toString(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataErroResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        logger.error("Erro interno inesperado", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", request.getRequestURI());
    }

    private ResponseEntity<DataErroResponse> buildErrorResponse(HttpStatus status, String message, String path) {
        DataErroResponse error = new DataErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return new ResponseEntity<>(error, status);
    }
}
