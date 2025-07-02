package com.ecommerce.aplication.records;

import com.ecommerce.model.users.TypeRole;

public record DataUsers(String name, String email, String password, TypeRole role) {
}
