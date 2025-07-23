package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.FavoriteProductRecords.DataFavoriteProductRequest;
import com.ecommerce.aplication.records.FavoriteProductRecords.DataFavoriteProductResponse;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.model.favorite.FavoriteProducts;
import com.ecommerce.model.product.CategoryItem;
import com.ecommerce.model.product.CategoryType;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.repositorys.FavoriteProductsRepository;
import com.ecommerce.model.repositorys.ProductRepository;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServiceFavoriteProducts {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFavoriteProducts.class);

    private final FavoriteProductsRepository favoRepo;
    private final ProductRepository productRepo;
    private final UsersRepositroy userRepo;

    public ServiceFavoriteProducts(FavoriteProductsRepository favoRepo, ProductRepository productRepo, UsersRepositroy userRepo) {
        this.favoRepo = favoRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    public void add(Long userId, DataFavoriteProductRequest request) {
        logger.info("Usuário {} tentando adicionar produto {} aos favoritos", userId, request.productId());

        if (favoRepo.findByUserIdAndProductId(userId, request.productId()).isPresent()) {
            logger.warn("Produto {} já está nos favoritos do usuário {}", request.productId(), userId);
            throw new BusinessRuleException("Produto já está nos favoritos.");
        }

        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + userId));
        ProductModel product = productRepo.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. ID: " + request.productId()));

        favoRepo.save(new FavoriteProducts(null, user, product, null));
        logger.info("Produto {} adicionado aos favoritos do usuário {}", request.productId(), userId);
    }

    public void remove(Long userId, Long productId) {
        logger.info("Usuário {} tentando remover produto {} dos favoritos", userId, productId);

        FavoriteProducts fav = favoRepo.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não está nos favoritos"));

        favoRepo.delete(fav);
        logger.info("Produto {} removido dos favoritos do usuário {}", productId, userId);
    }

    private DataFavoriteProductResponse toResponseDTO(FavoriteProducts fav) {
        return new DataFavoriteProductResponse(
                fav.getId(),
                fav.getUser().getName(),
                fav.getProduct().getName()
        );
    }

    public List<DataFavoriteProductResponse> recommend(Long userId) {
        List<FavoriteProducts> favoritos = favoRepo.findByUserId(userId);

        if (favoritos.isEmpty()) {
            throw new BusinessRuleException("Você ainda não possui favoritos para gerar recomendações.");
        }

        List<Long> produtosFavoritados = favoritos.stream()
                .map(fav -> fav.getProduct().getId())
                .toList();

        List<CategoryItem> categorias = favoritos.stream()
                .map(fav -> fav.getProduct().getItem())
                .distinct()
                .toList();

        List<CategoryType> tipos = favoritos.stream()
                .map(fav -> fav.getProduct().getType())
                .distinct()
                .toList();

        List<ProductModel> recomendados = productRepo
                .findByItemInAndTypeInAndIdNotIn(categorias, tipos, produtosFavoritados);

        return recomendados.stream()
                .map(prod -> new DataFavoriteProductResponse(null, null, prod.getName()))
                .toList();
    }


    public List<DataFavoriteProductResponse> list(Long userId) {
        logger.debug("Listando favoritos do usuário {}", userId);
        return favoRepo.findByUserId(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}