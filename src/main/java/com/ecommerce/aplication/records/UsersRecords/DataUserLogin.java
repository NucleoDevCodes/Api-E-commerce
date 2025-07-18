package com.ecommerce.aplication.records.UsersRecords;

import jakarta.validation.constraints.NotBlank;

public record DataUserLogin(@NotBlank String email,
                            @NotBlank String password) {
}
