package com.example.techtask.service.impl;

import com.example.techtask.dao.OrderDAO;
import com.example.techtask.model.Order;
import com.example.techtask.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderDAO orderDAO;

    @Autowired
    public OrderServiceImpl(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @Override
    public Order findOrder() {
        return orderDAO.findOrder().orElseThrow(
                () -> new RuntimeException("Order not found according to the conditions")); //todo custom exception
    }

    @Override
    public List<Order> findOrders() {
        List<Order> orderList = orderDAO.findOrders();
        if (orderList.isEmpty()) {
            throw new RuntimeException("Orders not found according to the conditions"); //todo custom exception
        }
        return orderList;
    }
}
