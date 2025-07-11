package com.ecommerce.aplication.records.CartRecords;

import com.ecommerce.model.cart.cartItem.CartItem;
import com.ecommerce.model.users.Users;

import java.util.List;

public record DataCart(Users users, List<CartItem> items) {
}
