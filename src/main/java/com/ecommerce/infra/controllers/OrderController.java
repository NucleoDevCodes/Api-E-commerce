package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.OrderRecords.DataOrderResponse;
import com.ecommerce.aplication.services.ServiceOrders;
import com.ecommerce.model.users.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


    @PostMapping("/checkout")
    public ResponseEntity<DataOrderResponse> checkout(@AuthenticationPrincipal Users user) {
        var response = serviceOrder.checkout(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario")
    public ResponseEntity<List<DataOrderResponse>> listUserOrders(@AuthenticationPrincipal Users user) {
        var list = serviceOrder.listOrdersByUser(user.getId());
        return ResponseEntity.ok(list);
    }
}
