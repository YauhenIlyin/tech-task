package com.example.techtask.dao.impl;

import com.example.techtask.dao.UserDAO;
import com.example.techtask.model.User;
import com.example.techtask.model.enumiration.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
public class UserDAOImpl implements UserDAO {
    private static final String SQL_QUERY_FIND_ONE_USER_BY_MAX_ORDERS_PRICE_DELIVERED_2003 = """
            SELECT u.id , u.email, u.user_status
            FROM users u 
            INNER JOIN orders o
            ON u.id = o.user_id
            AND o.created_at BETWEEN '1-January-03 00:00:00.000' AND '31-December-03 23:59:59.000'
            AND o."order_status" = 'DELIVERED'
            GROUP BY u.id
            ORDER BY sum(o.price) desc
            LIMIT 1;
            """;
    /**
     * If the order has been delivered, it means it has been paid.
     */
    private static final String SQL_QUERY_FIND_ALL_USERS_WITH_PAID_ORDER_2010 = """
            SELECT u.id , u.email, u.user_status
            FROM users u
            INNER JOIN orders o
            ON u.id = o.user_id
            AND o.created_at BETWEEN '01-January-10 00:00:00.000' AND '31-December-10 23:59:59.000'
            AND (o."order_status" = 'PAID' OR o."order_status" = 'DELIVERED')
            GROUP BY u.id;
            """;
    private final EntityManager em;

    @Autowired
    public UserDAOImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<User> findUser() {
        Query query = em.createNativeQuery(SQL_QUERY_FIND_ONE_USER_BY_MAX_ORDERS_PRICE_DELIVERED_2003);
        List<User> userList = buildUserListFromResultSet(query.getResultList());
        Optional<User> result = Optional.empty();
        if (!userList.isEmpty()) {
            result = Optional.of(userList.get(0));
        }
        return result;
    }

    @Override
    public List<User> findUsers() {
        Query query = em.createNativeQuery(SQL_QUERY_FIND_ALL_USERS_WITH_PAID_ORDER_2010);
        return buildUserListFromResultSet(query.getResultList());
    }

    private List<User> buildUserListFromResultSet(List<?> resultSetList) {
        List<User> userList = new ArrayList<>();
        resultSetList.forEach(o -> userList.add(buildFromResultSet((Object[]) o)));
        return userList;
    }

    private User buildFromResultSet(Object[] resultSet) {
        User user = new User();
        int id = (Integer) resultSet[0];
        String email = (String) resultSet[1];
        String userStatusValue = (String) resultSet[2];
        List<Field> fieldList = null;
        try {
            fieldList = getUserFieldList(user);
            ReflectionAPIAccessManager.openFieldsAccess(fieldList);
            fieldList.get(0).setInt(user, id);
            fieldList.get(1).set(user, email);
            fieldList.get(2).set(user, UserStatus.valueOf(userStatusValue));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            //todo log / custom exception
        } finally {
            ReflectionAPIAccessManager.closeFieldsAccess(fieldList);
        }
        return user;
    }

    private List<Field> getUserFieldList(User user) throws NoSuchFieldException {
        return List.of(
                user.getClass().getDeclaredField("id"),
                user.getClass().getDeclaredField("email"),
                user.getClass().getDeclaredField("userStatus"));
    }

}
