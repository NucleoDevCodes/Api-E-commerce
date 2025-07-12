package com.ecommerce.aplication.records.FavoriteProductRecords;

import java.util.UUID;

public record DataFavoriteProductResponse(  UUID id,
                                            String userName,
                                            String productName) {
}
