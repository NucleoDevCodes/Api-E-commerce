package com.ecommerce.aplication.records;

import com.ecommerce.model.users.TypeRole;

public record DataUserResponse(Long id, String username, String name, String email, TypeRole role) {
}
