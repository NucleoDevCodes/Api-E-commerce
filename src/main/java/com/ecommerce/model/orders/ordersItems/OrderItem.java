package com.ecommerce.model.orders.ordersItems;


import com.ecommerce.aplication.records.OrderRecords.DataOrderItems;
import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.product.ProductModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id",referencedColumnName = "id")
    @NotNull(message = "o pedido associado ao item não pode ser nulo")
    private OrderModel order;

    @ManyToOne
    @JoinColumn(name = "produto_id", referencedColumnName = "id")
    @NotNull(message = "O produto associado ao item não pode ser nulo.")
    private ProductModel product;

    @Column(name = "quantidade")
    @NotNull(message = "A quantidade deve ser informada.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Integer quantity;

    @NotNull(message = "Deve adcionar algum produto")
    private BigDecimal price;

    public OrderItem(DataOrderItems data){
        this.order=data.order();
        this.product=data.product();
        this.quantity=data.quantity();
        this.price=data.product().getPrice();
    }


}
