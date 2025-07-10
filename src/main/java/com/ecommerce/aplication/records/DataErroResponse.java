package com.ecommerce.aplication.records;

import java.time.LocalDateTime;

public record DataErroResponse(LocalDateTime timestamp,
                               int status,
                               String error,
                               String message,
                               String path) {
}
