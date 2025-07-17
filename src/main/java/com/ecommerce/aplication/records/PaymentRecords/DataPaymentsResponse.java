package com.ecommerce.aplication.records.PaymentRecords;

import com.ecommerce.model.payment.PaymentStatus;

import java.time.LocalDateTime;

public record DataPaymentsResponse
        (Long orderId,
         PaymentStatus status,
         LocalDateTime createdAt) {
}
