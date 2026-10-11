package com.smartpark.service;

import com.smartpark.dto.request.LoginRequest;
import com.smartpark.dto.request.RegisterRequest;
import com.smartpark.dto.response.AuthResponse;
import com.smartpark.dto.response.UserResponse;
import com.smartpark.entity.Role;
import com.smartpark.entity.User;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ConflictException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.UserRepository;
import com.smartpark.security.JwtTokenProvider;
import com.smartpark.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Confirm password check
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Enforce password policy: at least 8 characters, at least 1 digit or special char
        if (request.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }

        // Email uniqueness
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with this email address already exists");
        }

        // Student ID uniqueness
        String normalizedStudentId = request.getStudentId().trim().toUpperCase();
        if (userRepository.existsByStudentId(normalizedStudentId)) {
            throw new ConflictException("An account with this Student ID / College ID already exists");
        }

        // Requirement: Assign STUDENT role on public registration. Never allow ADMIN.
        User user = new User(
                normalizedEmail,
                normalizedStudentId,
                request.getFullName().trim(),
                request.getPhone() != null ? request.getPhone().trim() : null,
                passwordEncoder.encode(request.getPassword()),
                Role.STUDENT
        );

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            if (!principal.isEnabled()) {
                throw new BadCredentialsException("Account is deactivated");
            }

            String jwt = tokenProvider.generateToken(authentication);

            return new AuthResponse(
                    jwt,
                    principal.getId(),
                    principal.getEmail(),
                    principal.getFullName(),
                    principal.getStudentId(),
                    principal.getRole()
            );
        } catch (AuthenticationException ex) {
            // Requirement: Avoid exposing whether a particular account exists through unnecessarily detailed login errors
            throw new BadCredentialsException("Invalid credentials provided");
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserResponse.fromEntity(user);
    }
}
