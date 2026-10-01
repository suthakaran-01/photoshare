package com.photoshare.service;

import com.photoshare.dto.AuthResponse;
import com.photoshare.dto.LoginRequest;
import com.photoshare.dto.RegisterRequest;
import com.photoshare.entity.Role;
import com.photoshare.entity.User;
import com.photoshare.exception.ConflictException;
import com.photoshare.exception.UnauthorizedException;
import com.photoshare.repository.UserRepository;
import com.photoshare.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthResponse registerAdmin(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(Role.ADMIN);
        users.save(u);
        return new AuthResponse(jwtUtil.generate(u.getEmail(), u.getRole()), u.getRole(), u.getName());
    }

    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        return new AuthResponse(jwtUtil.generate(u.getEmail(), u.getRole()), u.getRole(), u.getName());
    }
}