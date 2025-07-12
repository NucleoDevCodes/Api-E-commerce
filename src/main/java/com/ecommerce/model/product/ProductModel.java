package com.ecommerce.model.product;

import com.ecommerce.aplication.records.ProductsRecords.DataProducts;
import com.ecommerce.model.favorite.FavoriteProducts;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "produtos")
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "nome", nullable = false)
    private String name;

    @NotNull
    @Positive
    @Column(name = "preco", nullable = false)
    private BigDecimal price;

    @NotNull
    @Min(1)
    @Column(name = "quantidade", nullable = false)
    private Integer quant;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "categoria_item", nullable = false)
    private CategoryItem item;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "categoria_tipo", nullable = false)
    private CategoryType type;

    @ElementCollection
    @CollectionTable(name = "produto_tamanhos", joinColumns = @JoinColumn(name = "produto_id"))
    @Column(name = "tamanho")
    private List<String> sizes;

    @ElementCollection
    @CollectionTable(name = "produto_cores", joinColumns = @JoinColumn(name = "produto_id"))
    @Column(name = "cor")
    private List<String> colors;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteProducts> favoritedByUsers = new ArrayList<>();

    public ProductModel(DataProducts data) {
        this.name = data.name();
        this.price = data.price();
        this.quant = data.quant();
        this.item = data.item();
        this.type = data.type();
        this.sizes = data.sizes();
        this.colors = data.colors();
    }
}
