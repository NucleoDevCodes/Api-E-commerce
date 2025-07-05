package com.ecommerce.aplication.records;

import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;

import java.math.BigDecimal;
import java.util.List;

public record DataProducts(   String name,
                              BigDecimal price,
                              String description,
                              CategoryItem item,
                              CategoryType type,
                              Integer quant,
                              List<String> sizes) {
}
