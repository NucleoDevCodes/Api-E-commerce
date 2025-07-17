package com.ecommerce.aplication.records.PaymentRecords;

import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.payment.PaymentStatus;

public record DataPayments(OrderModel order, PaymentStatus status) {
}
