package com.helpdesk.service;

import com.helpdesk.dto.*;
import com.helpdesk.model.User;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public java.util.Map<String, String> register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already in use");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .build();
        userRepo.save(user);
        return java.util.Map.of("message", "User registered successfully");
    }

    public JwtResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Authenticate against the underlying username-based UserDetailsService —
        // this keeps every other part of the app (JWT subject, @AuthenticationPrincipal
        // lookups in TicketService/CommentController, etc.) unchanged; only the
        // login form itself now takes an email instead of a username.
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), req.getPassword())
        );
        var userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtils.generateToken(userDetails);
        return new JwtResponse(token, user.getUsername(), user.getRole().name());
    }
}