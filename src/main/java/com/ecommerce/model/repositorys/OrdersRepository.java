package com.ecommerce.model.repositorys;

import com.ecommerce.model.orders.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<OrderModel,Long> {
    List<OrderModel> findByUsersId(Long userId);
}
