package com.ecommerce.model.product;

import com.ecommerce.aplication.records.DataProducts;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade não pode ser menor que 1")
    private Integer quant;

    @Enumerated(EnumType.STRING)
    private CategoryItem item;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @ElementCollection
    @CollectionTable(name = "produto_tamanho", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tamanho")
    private List<String> sizes;



    public ProductModel(DataProducts data) {
        this.name = data.name();
        this.price = data.price();
        this.quant = data.quant();
        this.item = data.item();
        this.type = data.type();
        this.sizes = data.sizes();
    }
}
