package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.request.ChangeNameRequest;
import com.example.demo.dto.request.ChangePasswordRequest;
import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;

public interface UserService {

    // ===== Registro y creación =====
    User register(RegisterRequest request);
    User createByAdmin(CreateUserRequest request);

    // ===== Consultas =====
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll();

    // ===== Modificaciones =====
    void changePassword(Long userId, ChangePasswordRequest request);
    void changeName(Long userId, ChangeNameRequest request);
    User updateRole(Long userId, Role role);

    // ===== Eliminación =====
    void deleteUser(Long id);
}