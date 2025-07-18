package com.ecommerce.aplication.records.UsersRecords;

import com.ecommerce.model.users.TypeRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DataUsersRegister(@NotBlank String name,
                                @NotBlank String email,
                                @NotBlank  String password,
                                @NotNull  TypeRole role) {
}
