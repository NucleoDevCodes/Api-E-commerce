package com.ecommerce.aplication.records.CartRecords;

import com.ecommerce.model.cart.CartModel;
import com.ecommerce.model.product.ProductModel;

public record DataCartItems(CartModel cart, ProductModel product, Integer quantity) {
}
