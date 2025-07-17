package com.ecommerce.model.payment;

import com.ecommerce.aplication.records.PaymentRecords.DataPayments;
import com.ecommerce.model.orders.OrderModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", referencedColumnName = "id", unique = true)
    private OrderModel order;

    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    private LocalDateTime timeCreated;

    public PaymentModel(DataPayments data) {
        this.order=data.order();
        this.status=data.status();
        this.timeCreated=LocalDateTime.now();

    }


}
