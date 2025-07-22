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
    private final  ServiceAsync serviceAsync;
    private final ServiceCart serviceCart;
    private final Random random = new Random();

    public ServicePayment(PaymentRepository paymentRepository, OrdersRepository orderRepository, ServiceAsync serviceAsync, ServiceCart serviceCart) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.serviceAsync = serviceAsync;
        this.serviceCart = serviceCart;
    }


    @Transactional
    public DataPaymentsResponse simulatePayment(Long orderId, Boolean forceFail) {
        OrderModel order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        PaymentModel payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            payment = new PaymentModel();
            payment.setOrder(order);
            payment.setStatus(PaymentStatus.PENDENTE);
            payment.setTimeCreated(LocalDateTime.now());
        }

        boolean fail = (forceFail != null) ? forceFail : new Random().nextInt(100) < 20;

        if (fail) {
            payment.setStatus(PaymentStatus.FALHOU);
        } else {
            payment.setStatus(PaymentStatus.PAGO);
            order.setStatus(OrderStatus.PAGO);
            orderRepository.save(order);
            serviceCart.finalizeCart(order.getUsers().getId());

            serviceAsync.sendConfirmationEmail(order);
        }

        payment = paymentRepository.save(payment);

        return new DataPaymentsResponse(payment.getOrder().getId(), payment.getStatus(), payment.getTimeCreated());
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

    public OrderModel getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}