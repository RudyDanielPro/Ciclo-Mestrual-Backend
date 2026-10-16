package com.example.demo.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.ChangeNameRequest;
import com.example.demo.dto.request.ChangePasswordRequest;
import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== Registro ====================

    @Override
    public User register(RegisterRequest request) {
        // Validar username único
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("El username ya está en uso");
        }

        // Validar email único
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("El email ya está en uso");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(Role.USER);   // forzado, siempre USER en registro público

        return userRepository.save(user);
    }

    // ==================== Creación por admin ====================

    @Override
    public User createByAdmin(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("El username ya está en uso");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("El email ya está en uso");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        return userRepository.save(user);
    }

    // ==================== Consultas ====================

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ==================== Modificaciones ====================

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);

        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual no es correcta");
        }

        // Validar que la nueva no sea igual
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void changeName(Long userId, ChangeNameRequest request) {
        User user = findById(userId);
        user.setName(request.getNewName());
        userRepository.save(user);
    }

    @Override
    public User updateRole(Long userId, Role role) {
        User user = findById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    // ==================== Eliminación ====================

    @Override
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}