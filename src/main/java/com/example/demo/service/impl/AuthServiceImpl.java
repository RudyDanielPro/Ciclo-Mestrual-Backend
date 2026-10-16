package com.example.demo.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.entity.User;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthServiceImpl(UserService userService,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager,
                           CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    // ==================== Registro ====================

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Registrar usuario (siempre USER) usando el servicio
        User user = userService.register(request);

        // Cargar el UserDetails del usuario recién creado
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // Generar token
        String token = jwtService.generateToken(userDetails);

        // Devolver respuesta
        return new AuthResponse(token, user.getUsername(), user.getName(), user.getRole());
    }

    // ==================== Login ====================

    @Override
    public AuthResponse login(LoginRequest request) {
        // Autenticar con Spring Security (lanza excepción si falla)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        // Si llegamos aquí, las credenciales son correctas.
        // Cargamos el usuario desde la BD
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getIdentifier());

        // Buscamos al usuario para obtener name y role
        User user = userService.findByUsername(userDetails.getUsername());

        // Generar token
        String token = jwtService.generateToken(userDetails);

        // Devolver respuesta
        return new AuthResponse(token, user.getUsername(), user.getName(), user.getRole());
    }
}