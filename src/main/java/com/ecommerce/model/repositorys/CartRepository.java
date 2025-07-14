package com.ecommerce.model.repositorys;

import com.ecommerce.model.cart.CartModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartModel,Long> {
    Optional<CartModel> findByUsersId(Long userId);

}
