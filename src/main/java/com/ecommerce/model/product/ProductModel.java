package com.ecommerce.model.product;

import com.ecommerce.aplication.records.DataProducts;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Data

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

    @Enumerated(EnumType.STRING)
    private  CategoryItem item;

    @Enumerated(EnumType.STRING)
    private  CategoryType type;



    public  ProductModel(DataProducts data){
        this.name=data.name();
        this.price=data.price();
        this.color= data.color();
        this.size= data.size();
        this.item=data.item();
        this.type=data.type();
    }
}
