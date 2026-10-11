package com.smartpark;

import com.smartpark.dto.request.LoginRequest;
import com.smartpark.dto.request.RegisterRequest;
import com.smartpark.dto.response.AuthResponse;
import com.smartpark.dto.response.UserResponse;
import com.smartpark.entity.Role;
import com.smartpark.entity.User;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ConflictException;
import com.smartpark.repository.UserRepository;
import com.smartpark.security.JwtTokenProvider;
import com.smartpark.security.UserPrincipal;
import com.smartpark.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 3600000L);
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, tokenProvider);
    }

    @Test
    @DisplayName("Should successfully register student with encrypted password and STUDENT role")
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("John Doe");
        req.setStudentId("STU-1001");
        req.setEmail("john@student.edu");
        req.setPassword("Password123!");
        req.setConfirmPassword("Password123!");

        when(userRepository.existsByEmail("john@student.edu")).thenReturn(false);
        when(userRepository.existsByStudentId("STU-1001")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedPassword");

        User savedUser = new User("john@student.edu", "STU-1001", "John Doe", null, "$2a$10$hashedPassword", Role.STUDENT);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("john@student.edu", res.getEmail());
        assertEquals("STU-1001", res.getStudentId());
        assertEquals(Role.STUDENT, res.getRole());
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    @DisplayName("Should reject registration if passwords do not match")
    void testRegisterPasswordMismatch() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("mismatch@test.edu");
        req.setPassword("Pass12345");
        req.setConfirmPassword("Different123");

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration if email already exists")
    void testRegisterDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("exists@student.edu");
        req.setStudentId("STU-9999");
        req.setPassword("Secret123!");
        req.setConfirmPassword("Secret123!");

        when(userRepository.existsByEmail("exists@student.edu")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully login and return JWT token")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("john@student.edu", "Password123!");
        User user = new User("john@student.edu", "STU-1001", "John Doe", null, "hash", Role.STUDENT);
        user.setId(1L);
        UserPrincipal principal = UserPrincipal.create(user);

        var auth = new UsernamePasswordAuthenticationToken(principal, "Password123!", principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertNotNull(res.getToken());
        assertTrue(tokenProvider.validateToken(res.getToken()));
        assertEquals("john@student.edu", tokenProvider.getUsernameFromToken(res.getToken()));
        assertEquals("john@student.edu", res.getEmail());
        assertEquals(Role.STUDENT, res.getRole());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid login")
    void testLoginInvalid() {
        LoginRequest req = new LoginRequest("bad@user.com", "wrongpass");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }
}
