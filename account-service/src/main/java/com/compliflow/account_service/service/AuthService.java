package com.compliflow.account_service.service;

import com.compliflow.account_service.dto.LoginRequestDto;
import com.compliflow.account_service.dto.RegisterRequestDto;
import com.compliflow.account_service.model.Role;
import com.compliflow.account_service.model.User;
import com.compliflow.account_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = User.builder().email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER).build();

        userRepository.save(user);
    }

    public String login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));


        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return jwtService.generateToken(user);
    }
}
