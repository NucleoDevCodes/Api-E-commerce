package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.PaymentRecords.DataPaymentsRequest;
import com.ecommerce.aplication.records.PaymentRecords.DataPaymentsResponse;
import com.ecommerce.aplication.services.ServicePayment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pagamento")
public class PaymentController {
    private final ServicePayment service;

    public PaymentController(ServicePayment service) {
        this.service = service;
    }

    @PostMapping("/{pedidoId}")
    public ResponseEntity<DataPaymentsResponse> simulate(@PathVariable("pedidoId") Long pedidoId, @RequestBody(required = false) DataPaymentsRequest request){
        Boolean forceFail =request != null ? request.forceFail(): null;
        DataPaymentsResponse response= service.simulatePayment(pedidoId,forceFail);
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<DataPaymentsResponse> getPaymentStatus(@PathVariable Long pedidoId) {
        DataPaymentsResponse response = service.getPaymentStatus(pedidoId);
        return ResponseEntity.ok(response);
    }
}
