package com.example.techtask.service.impl;

import com.example.techtask.dao.UserDAO;
import com.example.techtask.model.User;
import com.example.techtask.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Used RuntimeException instead of Exception to save task requirements.
     */
    @Override
    public User findUser() {
        return userDAO.findUser().orElseThrow(
                () -> new RuntimeException("User not found according to the conditions")); //todo custom exception
    }

    @Override
    public List<User> findUsers() {
        List<User> userList = userDAO.findUsers();
        if (userList.isEmpty()) {
            throw new RuntimeException("Users not found according to the conditions"); //todo custom exception
        }
        return userList;
    }
}
