package com.ecommerce.model.repositorys;

import com.ecommerce.model.favorite.FavoriteProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteProductsRepository extends JpaRepository<FavoriteProducts, UUID> {
    List<FavoriteProducts> findByUserId(Long userId);
    Optional<FavoriteProducts> findByUserIdAndProductId(Long userId, Long productId);
}
