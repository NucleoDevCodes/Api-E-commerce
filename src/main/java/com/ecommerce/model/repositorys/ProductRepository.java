package com.ecommerce.model.repositorys;

import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel,Long> {
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ProductModel p
        JOIN p.colors c
        JOIN p.sizes s
        WHERE p.name = :name
          AND c = :color
          AND s = :size
    """)
    boolean existsByNameAndColorAndSize(
            @Param("name") String name,
            @Param("color") String color,
            @Param("size") String size
    );


    @Query("""
            SELECT p from ProductModel p 
            Where p.item =: categoryItem
            AND p.id <>: productId
            ORDER BY function('RAND')
            
            """)

List<ProductModel>findTop6ByItemAndNotIdRandomOrder(
        @Param("categoryItem")CategoryItem categoryItem,
        @Param("productId") Long productId);

    Page<ProductModel> findBySizesContainingIgnoreCase(String sizes, Pageable pageable);
    Page<ProductModel> findByColorsContainingIgnoreCase(String colors, Pageable pageable);

    Page<ProductModel> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<ProductModel> findByItem(CategoryItem item, Pageable pageable);

    Page<ProductModel> findByType(CategoryType type, Pageable pageable);

    List<ProductModel> findByItemInAndTypeInAndIdNotIn(List<CategoryItem> items, List<CategoryType> types, List<Long> excludedIds);


    Page<ProductModel> findByItemAndType(CategoryItem item, CategoryType type, Pageable pageable);
}
