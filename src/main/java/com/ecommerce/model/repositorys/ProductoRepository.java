package com.ecommerce.model.repositorys;

import com.ecommerce.model.product.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<ProductModel,Long> {
    boolean existsByNameAndColorAndSize(String name, String color, String size);
}
