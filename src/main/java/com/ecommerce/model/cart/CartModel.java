package com.ecommerce.model.cart;

import com.ecommerce.aplication.records.CartRecords.DataCart;
import com.ecommerce.model.cart.cartItem.CartItem;
import com.ecommerce.model.users.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "carrinho")
public class CartModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    @NotNull(message = "O usuário associado ao carrinho não pode ser nulo.")
    private Users users;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull(message = "A lista de itens do carrinho não pode ser nula.")
    private List<CartItem> items = new ArrayList<>();

    public CartModel(DataCart data) {
        this.users = data.users();
        this.items = data.items();
    }


}
