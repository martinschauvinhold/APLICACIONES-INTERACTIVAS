package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Optional;

import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.UserRequest;

public interface UserService {
    public ArrayList<User> getUsers();

    public Optional<User> getUserById(int userId);

    public User createUser(UserRequest userRequest);

    public User updateUser(int userId, UserRequest userRequest);

    public void deleteUser(int userId);
}
