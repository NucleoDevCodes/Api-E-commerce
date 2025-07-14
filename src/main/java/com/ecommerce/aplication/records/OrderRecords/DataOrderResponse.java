package com.ecommerce.aplication.records.OrderRecords;

import com.ecommerce.model.orders.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DataOrderResponse(Long orderId,
                                OrderStatus status,
                                LocalDateTime createdAt,
                                List<DataOrderItemResponse> items) {
}
