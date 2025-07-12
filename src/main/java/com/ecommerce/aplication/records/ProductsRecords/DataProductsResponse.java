package com.ecommerce.aplication.records.ProductsRecords;

import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;

import java.math.BigDecimal;
import java.util.List;

public record DataProductsResponse(Long id,
                                   String name,
                                   BigDecimal price,
                                   Integer quant,
                                   CategoryItem item,
                                   CategoryType type,
                                   List<String> sizes,
                                   List<String> colors) {
}
