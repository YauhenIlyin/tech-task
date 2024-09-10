package com.example.techtask.dao.impl;

import com.example.techtask.dao.OrderDAO;
import com.example.techtask.model.Order;
import com.example.techtask.model.enumiration.OrderStatus;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Unfortunately I had to use the Reflection API.
 * Spring Data JPA cannot be used according to the terms of the task,
 * and entities are closed for initialization and modification.
 * MANDATORY CONDITION:
 * - It is prohibited to change any files except DI and calling service methods in the
 * OrderController/UserController files and files located in the /service/impl folder (which you will add).
 * Note:
 * The files in the dao package were not modified, but created.
 */
@Component
public class OrderDAOImpl implements OrderDAO {
    private static final String SQL_QUERY_FIND_NEWEST_ORDER_BY_QUANTITY_MORE_TWO = """
            SELECT *
            FROM orders o
            WHERE quantity > 1
            ORDER BY o.created_at desc
            LIMIT 1;
            """;
    private static final String SQL_QUERY_FIND_ORDERS_OF_ACTIVE_USERS_ASC_BY_CREATED_AT = """
            select o.id, o.product_name, o.price, o.quantity, o.user_id, o.created_at, o.order_status
                                  from orders o
                                  inner join users u
                                  on o.user_id = u.id\s
                                  and u."user_status" = 'ACTIVE'
                                  order by o.created_at;
                                  
            """;
    private final EntityManager em;

    @Autowired
    public OrderDAOImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<Order> findOrder() {
        Query query = em.createNativeQuery(SQL_QUERY_FIND_NEWEST_ORDER_BY_QUANTITY_MORE_TWO);
        List<Order> orderList = buildOrderListFromResultSet(query.getResultList());
        Optional<Order> result = Optional.empty();
        if (!orderList.isEmpty()) {
            result = Optional.of(orderList.get(0));
        }
        return result;
    }

    @Override
    public List<Order> findOrders() {
        Query query = em.createNativeQuery(SQL_QUERY_FIND_ORDERS_OF_ACTIVE_USERS_ASC_BY_CREATED_AT);
        return buildOrderListFromResultSet(query.getResultList());
    }

    private List<Order> buildOrderListFromResultSet(List<?> resultSetList) {
        List<Order> orderList = new ArrayList<>();
        for (Object orderResultSet : resultSetList) {
            orderList.add(buildFromResultSet((Object[]) orderResultSet));
        }
        return orderList;
    }

    private Order buildFromResultSet(Object[] resultSet) {
        Order order = new Order();
        int id = (Integer) resultSet[0];
        String productName = (String) resultSet[1];
        double price = ((BigDecimal) resultSet[2]).doubleValue();
        int quantity = (Integer) resultSet[3];
        int userId = (Integer) resultSet[4];
        Timestamp timestampCreatedAt = (Timestamp) resultSet[5];
        LocalDateTime createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(
                        timestampCreatedAt.getTime()),
                ZoneId.systemDefault());
        OrderStatus orderStatus = OrderStatus.valueOf((String) resultSet[6]);
        List<Field> fieldList = null;
        try {
            fieldList = getOrderFieldList(order);
            ReflectionAPIAccessManager.openFieldsAccess(fieldList);
            fieldList.get(0).setInt(order, id);
            fieldList.get(1).set(order, productName);
            fieldList.get(2).setDouble(order, price);
            fieldList.get(3).setInt(order, quantity);
            fieldList.get(4).setInt(order, userId);
            fieldList.get(5).set(order, createdAt);
            fieldList.get(6).set(order, orderStatus);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            //todo log / custom exception
        } finally {
            ReflectionAPIAccessManager.closeFieldsAccess(fieldList);
        }
        return order;
    }

    private List<Field> getOrderFieldList(Order order) throws NoSuchFieldException {
        return List.of(
                order.getClass().getDeclaredField("id"),
                order.getClass().getDeclaredField("productName"),
                order.getClass().getDeclaredField("price"),
                order.getClass().getDeclaredField("quantity"),
                order.getClass().getDeclaredField("userId"),
                order.getClass().getDeclaredField("createdAt"),
                order.getClass().getDeclaredField("orderStatus"));
    }
}
