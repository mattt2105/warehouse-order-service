package com.order_service.order_service.controller;

import com.order_service.order_service.model.Order;
import com.order_service.order_service.service.OrderService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PutMapping("/create")
    public ResponseEntity<Order> addProduct(@RequestParam @NotNull Long id, @RequestParam @NotNull @Min(1) int quantity){
        Order addOrder = orderService.addOrder(id, quantity);
        return new ResponseEntity<>(addOrder, HttpStatus.CREATED);
    }
}
