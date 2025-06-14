package com.ecommerce.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @Positive(message = "O preço deve ser maior que zero")
    @NotNull(message = "O preço é obrigatório")
    private BigDecimal price;

    @NotBlank(message = "A cor é obrigatória")
    private String color;

    @NotBlank(message = "O tamanho é obrigatório")
    private String size;
}
