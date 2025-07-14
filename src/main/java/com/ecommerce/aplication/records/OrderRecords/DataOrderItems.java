package com.ecommerce.aplication.records.OrderRecords;

import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.product.ProductModel;

public record DataOrderItems(OrderModel order, ProductModel product, Integer quantity) {
}
