package com.ecommerce.aplication.records.CartRecords;

public record DataCartItemRequest(Long productId, Integer quantity, String color, String size) {
}
