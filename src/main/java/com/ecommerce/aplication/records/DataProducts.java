package com.ecommerce.aplication.records;

import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;

import java.math.BigDecimal;

public record DataProducts(String name, BigDecimal price , String color , String size, CategoryItem item,CategoryType type) {
}
