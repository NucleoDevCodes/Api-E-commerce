package com.ecommerce.model.cart.cartItem;

import com.ecommerce.aplication.records.CartRecords.DataCartItems;
import com.ecommerce.model.cart.CartModel;
import com.ecommerce.model.product.ProductModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "itens_carrinho")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "carrinho_id", referencedColumnName = "id")
    @NotNull(message = "O carrinho associado ao item não pode ser nulo.")
    private CartModel cart;

    @ManyToOne
    @JoinColumn(name = "produto_id", referencedColumnName = "id")
    @NotNull(message = "O produto associado ao item não pode ser nulo.")
    private ProductModel product;

    @Column(name = "quantidade")
    @NotNull(message = "A quantidade deve ser informada.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Integer quantity;

    @Column(name = "cor")
    private String color;

    @Column(name = "tamanho")
    private String size;


    public CartItem(DataCartItems data) {
        this.cart = data.cart();
        this.product = data.product();
        this.quantity = data.quantity();
        this.color=data.color();
        this.size=data.size();
    }
}
