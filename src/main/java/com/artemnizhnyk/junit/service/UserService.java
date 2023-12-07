package com.artemnizhnyk.junit.service;

import com.artemnizhnyk.junit.dao.UserDao;
import com.artemnizhnyk.junit.dto.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class UserService {
    private final List<User> users = new ArrayList<>();
    private final UserDao userDao;

    public UserService(final UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean delete(Integer userId) {
        return userDao.delete(userId);
    }

    public List<User> getAll() {
        return users;
    }

    public void add(final User... users) {
        this.users.addAll(Arrays.asList(users));
    }

    public Optional<User> login(final String username, final String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("username or password is null ");
        }
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .filter(user -> user.getPassword().equals(password))
                .findFirst();
    }

    public Map<Integer, User> getAllConvertedById() {
        return users.stream()
                .collect(toMap(User::getId, Function.identity()));
    }
}
