package com.ecommerce.model.favorite;

import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.users.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "produtos_favoritos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuarios_id","produto_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteProducts {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "usuarios_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private ProductModel product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
