package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.DataFavoriteProductRequest;
import com.ecommerce.aplication.records.DataFavoriteProductResponse;
import com.ecommerce.model.favorite.FavoriteProducts;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.FavoriteProductsRepository;
import com.ecommerce.model.repositorys.ProductRepository;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.Users;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceFavoriteProducts {
    private final FavoriteProductsRepository favoRepo;
    private final ProductRepository productRepo;
    private final UsersRepositroy userRepo;

    public ServiceFavoriteProducts(FavoriteProductsRepository favoRepo, ProductRepository productRepo, UsersRepositroy userRepo) {
        this.favoRepo = favoRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    public void add(Long userId, DataFavoriteProductRequest request) {
        if (favoRepo.findByUserIdAndProductId(userId, request.productId()).isPresent())
            throw new IllegalArgumentException("Produto já nos favoritos");

        Users user = userRepo.findById(userId).orElseThrow();
        ProductModel product = productRepo.findById(request.productId()).orElseThrow();

        favoRepo.save(new FavoriteProducts(null, user, product, null));
    }

    public void remove(Long userId, Long productId) {
        FavoriteProducts favo =
                favoRepo.findByUserIdAndProductId(userId, productId)
                        .orElseThrow(() -> new EntityNotFoundException("Produto não está nos favoritos"));

        favoRepo.delete(favo);
    }

    public List<DataFavoriteProductResponse> list(Long userId) {
        return favoRepo.findByUserId(userId).stream()
                .map(f ->
                        new DataFavoriteProductResponse(f.getId(), f.getProduct().getId(), f.getProduct().getName()))
                .collect(Collectors.toList());
    }
}
