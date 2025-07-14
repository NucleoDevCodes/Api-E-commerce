package com.ecommerce.aplication.records.OrderRecords;

import com.ecommerce.model.users.Users;

import java.util.List;

public record DataOrder(Users users, List<DataOrder> items) {
}
