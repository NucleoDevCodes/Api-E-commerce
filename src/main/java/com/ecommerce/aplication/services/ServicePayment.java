package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.PaymentRecords.DataPaymentsResponse;
import com.ecommerce.infra.exceptions.OrderNotFoundException;
import com.ecommerce.infra.exceptions.PaymentNotFoundException;
import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.orders.OrderStatus;
import com.ecommerce.model.payment.PaymentModel;
import com.ecommerce.model.payment.PaymentStatus;
import com.ecommerce.model.repositorys.OrdersRepository;
import com.ecommerce.model.repositorys.PaymentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class ServicePayment {

    private final Logger logger= LoggerFactory.getLogger(ServicePayment.class);
    private final PaymentRepository paymentRepository;
    private final OrdersRepository orderRepository;
    private final ServiceCart serviceCart;
    private final Random random = new Random();

    public ServicePayment(PaymentRepository paymentRepository, OrdersRepository orderRepository, ServiceCart serviceCart) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.serviceCart = serviceCart;
    }


    @Transactional
    public DataPaymentsResponse simulatePayment(Long orderId, Boolean forceFail) {
        logger.info("Iniciando simulação de pagamento para pedido ID: {}", orderId);

        OrderModel order = orderRepository.findById(orderId).orElseThrow(() -> {
            logger.warn("Pedido não encontrado para pagamento, ID: {}", orderId);
            return new OrderNotFoundException(orderId);
        });

        PaymentModel payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> {
            logger.warn("Pagamento não encontrado para pedido ID: {}", orderId);
            return new OrderNotFoundException(orderId);
        });

        payment.setStatus(PaymentStatus.PENDENTE);
        payment.setTimeCreated(LocalDateTime.now());

        boolean fail;
        if (forceFail != null) {
            fail = forceFail;
            logger.debug("Forçando falha no pagamento: {}", fail);
        } else {
            fail = random.nextInt(100) < 20;
            logger.debug("Falha aleatória no pagamento: {}", fail);
        }

        if (fail) {
            payment.setStatus(PaymentStatus.FALHOU);
            logger.info("Pagamento falhou para pedido ID: {}", orderId);
        } else {
            payment.setStatus(PaymentStatus.PAGO);
            order.setStatus(OrderStatus.PAGO);
            orderRepository.save(order);
            logger.info("Pagamento concluído com sucesso para pedido ID: {}", orderId);

            serviceCart.finalizeCart(order.getUsers().getId());
            logger.info("Carrinho finalizado para usuário ID: {}", order.getUsers().getId());
        }

        var savedPayment = paymentRepository.save(payment);
        logger.debug("Pagamento salvo com status {} para pedido ID: {}", savedPayment.getStatus(), orderId);

        return new DataPaymentsResponse(
                savedPayment.getOrder().getId(),
                savedPayment.getStatus(),
                savedPayment.getTimeCreated()
        );
    }

    public DataPaymentsResponse getPaymentStatus(Long orderId) {
        logger.debug("Consultando status do pagamento para pedido ID: {}", orderId);

        PaymentModel paymentModel = paymentRepository.findByOrderId(orderId).orElseThrow(() -> {
            logger.warn("Pagamento não encontrado para pedido ID: {}", orderId);
            return new PaymentNotFoundException(orderId);
        });

        return new DataPaymentsResponse(
                paymentModel.getOrder().getId(),
                paymentModel.getStatus(),
                paymentModel.getTimeCreated()
        );
    }
}