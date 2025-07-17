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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class ServicePayment {
    private final PaymentRepository paymentRepository;
    private final OrdersRepository orderRepository;
    private final Random random = new Random();

    public ServicePayment(PaymentRepository paymentRepository, OrdersRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }


    @Transactional
    public DataPaymentsResponse simulatePayment(Long orderId, Boolean forceFail) {
        OrderModel order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        PaymentModel payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));


        payment.setStatus(PaymentStatus.PENDENTE);
        payment.setTimeCreated(LocalDateTime.now());

        boolean fail;
        if (forceFail != null) {
            fail = forceFail;
        } else {
            fail = random.nextInt(100) < 20;
        }

        if (fail) {
            payment.setStatus(PaymentStatus.FALHOU);
        } else {
            payment.setStatus(PaymentStatus.PAGO);
            order.setStatus(OrderStatus.PAGO);
            orderRepository.save(order);
        }

        var savedPayment = paymentRepository.save(payment);

        return new DataPaymentsResponse(
                savedPayment.getOrder().getId(),
                savedPayment.getStatus(),
                savedPayment.getTimeCreated()
        );
    }

    public DataPaymentsResponse getPaymentStatus(Long orderId) {
        PaymentModel paymentModel =
                paymentRepository.
        findByOrderId(orderId).orElseThrow(() -> new PaymentNotFoundException(orderId));

        return new DataPaymentsResponse(
                paymentModel.getOrder().getId(),
                paymentModel.getStatus(),
                paymentModel.getTimeCreated()
        );
    }
}
