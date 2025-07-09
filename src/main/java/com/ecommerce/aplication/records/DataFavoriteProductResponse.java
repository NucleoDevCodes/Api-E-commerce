package com.ecommerce.aplication.records;

import java.util.UUID;

public record DataFavoriteProductResponse(  UUID id,
                                            String userName,
                                            String productName) {
}
