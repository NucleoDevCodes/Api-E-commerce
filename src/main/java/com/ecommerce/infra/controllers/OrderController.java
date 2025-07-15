package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.OrderRecords.DataOrderResponse;
import com.ecommerce.aplication.services.ServiceOrders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/pedido")
public class OrderController {

    private final ServiceOrders serviceOrder;

    public OrderController(ServiceOrders serviceOrder) {
        this.serviceOrder = serviceOrder;
    }

    private Long getLoggedUserId() {
        return 1L;
    }

    @PostMapping("/checkout")
    public ResponseEntity<DataOrderResponse> checkout() {
        var response = serviceOrder.checkout(getLoggedUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario")
    public ResponseEntity<List<DataOrderResponse>> getUserOrders() {
        var list = serviceOrder.listOrdersByUser(getLoggedUserId());
        return ResponseEntity.ok(list);
    }
}
