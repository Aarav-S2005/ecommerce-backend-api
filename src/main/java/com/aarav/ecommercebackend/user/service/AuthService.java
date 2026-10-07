package com.aarav.ecommercebackend.user.service;

import com.aarav.ecommercebackend.security.JwtTokenProvider;
import com.aarav.ecommercebackend.security.UserPrincipal;
import com.aarav.ecommercebackend.user.dto.request.LoginRequestDTO;
import com.aarav.ecommercebackend.user.dto.request.RegisterRequestDTO;
import com.aarav.ecommercebackend.user.dto.response.LoginResponseDTO;
import com.aarav.ecommercebackend.user.dto.response.RegisterResponseDTO;
import com.aarav.ecommercebackend.user.entity.Role;
import com.aarav.ecommercebackend.user.entity.User;
import com.aarav.ecommercebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String AUTH_COOKIE_NAME = "auth-token";
    public static final Duration COOKIE_EXPIRATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public record AuthResponse(LoginResponseDTO response, ResponseCookie cookie) {}

    public RegisterResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponseDTO(
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getCreatedAt()
        );
    }

    public AuthResponse login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateJwtToken(authentication);
        ResponseCookie cookie = createAuthCookie(token);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + principal.getId()));

        LoginResponseDTO loginResponse = new LoginResponseDTO(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone()
        );

        return new AuthResponse(loginResponse, cookie);
    }

    public ResponseCookie logout() {
        return createCleanCookie();
    }

    public ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // Set to true in production when running on HTTPS
                .path("/")
                .maxAge(COOKIE_EXPIRATION)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createCleanCookie() {
        return ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
