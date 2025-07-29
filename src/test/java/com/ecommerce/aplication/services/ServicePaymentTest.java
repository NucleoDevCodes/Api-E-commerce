package com.ecommerce.aplication.services;

import static org.junit.jupiter.api.Assertions.*;
import com.ecommerce.aplication.records.PaymentRecords.DataPaymentsResponse;
import com.ecommerce.infra.exceptions.OrderNotFoundException;
import com.ecommerce.infra.exceptions.PaymentNotFoundException;
import com.ecommerce.model.orders.OrderModel;
import com.ecommerce.model.orders.OrderStatus;
import com.ecommerce.model.payment.PaymentModel;
import com.ecommerce.model.payment.PaymentStatus;
import com.ecommerce.model.repositorys.OrdersRepository;
import com.ecommerce.model.repositorys.PaymentRepository;
import com.ecommerce.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicePaymentTest {


    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private ServiceAsync serviceAsync;

    @Mock
    private ServiceCart serviceCart;

    @InjectMocks
    private ServicePayment servicePayment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private OrderModel createOrder(Long id) {
        OrderModel order = new OrderModel();
        order.setId(id);
        order.setStatus(OrderStatus.PENDENTE);

        Users user = new Users();
        user.setId(123L);
        order.setUsers(user);

        return order;
    }

    private PaymentModel createPayment(OrderModel order, PaymentStatus status) {
        PaymentModel payment = new PaymentModel();
        payment.setOrder(order);
        payment.setStatus(status);
        payment.setTimeCreated(LocalDateTime.now());
        return payment;
    }

    @Test
    void simulatePayment_PaymentCreatedAndSuccess() {
        Long orderId = 1L;
        OrderModel order = createOrder(orderId);

        when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentModel.class))).thenAnswer(i -> i.getArgument(0));

        DataPaymentsResponse response = servicePayment.simulatePayment(orderId, false);

        assertEquals(orderId, response.orderId());
        assertEquals(PaymentStatus.PAGO, response.status());
        verify(ordersRepository).save(order);
        verify(serviceCart).finalizeCart(order.getUsers().getId());
        verify(serviceAsync).sendConfirmationEmail(order);
    }

    @Test
    void simulatePayment_PaymentExistsAndFails() {
        Long orderId = 1L;
        OrderModel order = createOrder(orderId);
        PaymentModel existingPayment = createPayment(order, PaymentStatus.PENDENTE);

        when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(PaymentModel.class))).thenAnswer(i -> i.getArgument(0));

        DataPaymentsResponse response = servicePayment.simulatePayment(orderId, true);

        assertEquals(orderId, response.orderId());
        assertEquals(PaymentStatus.FALHOU, response.status());
        verify(ordersRepository, never()).save(any());
        verify(serviceCart, never()).finalizeCart(any());
        verify(serviceAsync, never()).sendConfirmationEmail(any());
    }

    @Test
    void simulatePayment_OrderNotFound() {
        Long orderId = 999L;

        when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> servicePayment.simulatePayment(orderId, false));

        assertTrue(ex.getMessage().contains(orderId.toString()));
    }

    @Test
    void getPaymentStatus_Success() {
        Long orderId = 1L;
        OrderModel order = createOrder(orderId);
        PaymentModel payment = createPayment(order, PaymentStatus.PAGO);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        DataPaymentsResponse response = servicePayment.getPaymentStatus(orderId);

        assertEquals(orderId, response.orderId());
        assertEquals(PaymentStatus.PAGO, response.status());
    }

    @Test
    void getPaymentStatus_NotFound() {
        Long orderId = 1L;

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        PaymentNotFoundException ex = assertThrows(PaymentNotFoundException.class,
                () -> servicePayment.getPaymentStatus(orderId));

        assertTrue(ex.getMessage().contains(orderId.toString()));
    }

    @Test
    void getOrderById_Success() {
        Long orderId = 1L;
        OrderModel order = createOrder(orderId);

        when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderModel result = servicePayment.getOrderById(orderId);

        assertEquals(orderId, result.getId());
    }

    @Test
    void getOrderById_NotFound() {
        Long orderId = 1L;

        when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> servicePayment.getOrderById(orderId));

        assertTrue(ex.getMessage().contains(orderId.toString()));
    }

}