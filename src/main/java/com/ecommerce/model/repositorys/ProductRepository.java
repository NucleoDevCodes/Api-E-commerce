package com.ecommerce.model.repositorys;

import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel,Long> {
    boolean existsByNameAndColorAndSize(String name, String color, String size);
    Page<ProductModel> findBySizeIgnoreCase(String size, Pageable pageable);



    Page<ProductModel> findByItem(CategoryItem item, Pageable pageable);

    Page<ProductModel> findByType(CategoryType type, Pageable pageable);

    Page<ProductModel> findByItemAndType(CategoryItem item, CategoryType type, Pageable pageable);
}
