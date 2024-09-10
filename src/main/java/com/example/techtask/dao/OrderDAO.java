package com.example.techtask.dao;

import com.example.techtask.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Optional<Order> findOrder();

    List<Order> findOrders();
}
