package com.ecommerce.aplication.records.OrderRecords;

import java.math.BigDecimal;

public record DataOrderItemResponse(    Long productId,
                                        String productName,
                                        Integer quantity,
                                        BigDecimal price,
                                        String color,
                                        String size) {
}
