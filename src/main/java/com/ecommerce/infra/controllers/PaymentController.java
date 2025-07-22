package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.PaymentRecords.DataPaymentsRequest;
import com.ecommerce.aplication.records.PaymentRecords.DataPaymentsResponse;
import com.ecommerce.aplication.services.ServiceAsync;
import com.ecommerce.aplication.services.ServicePayment;
import com.ecommerce.model.users.Users;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pagamento")
public class PaymentController {
    private final ServicePayment service;
    private final ServiceAsync serviceAsync;

    public PaymentController(ServicePayment service, ServiceAsync serviceAsync) {
        this.service = service;
        this.serviceAsync = serviceAsync;
    }


    @PostMapping(value = "/{pedidoId}", consumes = "application/json")
    public ResponseEntity<DataPaymentsResponse> simulate(
            @AuthenticationPrincipal Users user,
            @PathVariable("pedidoId") Long pedidoId,
            @Valid @RequestBody(required = false) DataPaymentsRequest request) {

        Boolean forceFail = request != null ? request.forceFail() : null;
        DataPaymentsResponse response = service.simulatePayment(pedidoId, forceFail);


        if (response.status().equals("PAGO")) {
            serviceAsync.sendConfirmationEmail(service.getOrderById(pedidoId));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<DataPaymentsResponse> getPaymentStatus(
            @AuthenticationPrincipal Users user,
            @PathVariable Long pedidoId) {
        DataPaymentsResponse response = service.getPaymentStatus(pedidoId);
        return ResponseEntity.ok(response);
    }
}
