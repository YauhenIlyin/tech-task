package com.example.techtask.dao;

import com.example.techtask.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findUser();

    List<User> findUsers();
}
