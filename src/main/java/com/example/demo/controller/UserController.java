package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.ChangeNameRequest;
import com.example.demo.dto.request.ChangePasswordRequest;
import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtils;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== Usuario autenticado ====================

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        User user = getCurrentUser();
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changeMyPassword(@RequestBody ChangePasswordRequest request) {
        User user = getCurrentUser();
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @PutMapping("/me/name")
    public ResponseEntity<UserResponse> changeMyName(@RequestBody ChangeNameRequest request) {
        User user = getCurrentUser();
        userService.changeName(user.getId(), request);
        User updated = userService.findById(user.getId());
        return ResponseEntity.ok(toResponse(updated));
    }

    // ==================== Admin ====================

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createByAdmin(request);
        return new ResponseEntity<>(toResponse(user), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                   @RequestBody Role role) {
        User user = userService.updateRole(id, role);
        return ResponseEntity.ok(toResponse(user));
    }

    // ==================== Helpers ====================

    private User getCurrentUser() {
        String username = SecurityUtils.getAuthenticatedUsername();
        return userService.findByUsername(username);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}