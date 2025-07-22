package com.ecommerce.aplication.services;

import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.product.ProductModel;
import com.ecommerce.model.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ServiceAsync {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAsync.class);

    private final ServiceAsync self;

    public ServiceAsync(@Lazy ServiceAsync self) {
        this.self = self;
    }

    @Async("taskExecutor")
    public void sendConfirmationEmail(OrderModel order) {
        logger.info("⏳ Enviando e-mail de confirmação para pedido: {} na thread: {}", order.getId(), Thread.currentThread().getName());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("✅ E-mail enviado para pedido: {}", order.getId());
    }

    @Async("taskExecutor")
    public void updateRecommendations(ProductModel product) {
        logger.info("⏳ Atualizando recomendações para produto: {} na thread: {}", product.getId(), Thread.currentThread().getName());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("✅ Recomendações atualizadas para produto: {}", product.getId());
    }

    @Async("taskExecutor")
    public void sendWelcomeEmail(Users user) {
        logger.info("⏳ Enviando boas-vindas para: {} na thread: {}", user.getEmail(), Thread.currentThread().getName());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("✅ E-mail de boas-vindas enviado para: {}", user.getEmail());
    }

    @Async("taskExecutor")
    public void updateRecommendationsForOrder(OrderModel order) {
        logger.info("⏳ Atualizando recomendações com base no pedido: {} na thread: {}", order.getId(), Thread.currentThread().getName());
        try {
            order.getItems().forEach(orderItem -> {
                ProductModel product = orderItem.getProduct();
                logger.info("↪️ Disparando update async para produto ID: {}", product.getId());
                self.updateRecommendations(product);
            });
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar recomendações para o pedido: {}", order.getId(), e);
        }
        logger.info("✅ Atualização de recomendações finalizada para pedido: {}", order.getId());
    }
}