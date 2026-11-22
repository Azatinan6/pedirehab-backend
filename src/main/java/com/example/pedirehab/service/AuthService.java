package com.example.pedirehab.service;

import com.example.pedirehab.dto.AuthResponse;
import com.example.pedirehab.dto.LoginRequest;
import com.example.pedirehab.dto.RegisterTherapistRequest;
import com.example.pedirehab.entity.User;
import com.example.pedirehab.repository.UserRepository;
import com.example.pedirehab.security.CustomUserDetailsService;
import com.example.pedirehab.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse registerTherapist(RegisterTherapistRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçerli bir kullanıcı adı ve en az 6 karakterli şifre girin.");
        }

        if (userRepository.existsByUsername(request.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten kayıtlı.");
        }

        User therapist = User.builder()
                .username(request.username().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("THERAPIST")
                .fullName(request.fullName())
                .isActive(true)
                .build();

        userRepository.save(therapist);

        UserDetails userDetails = userDetailsService.loadUserByUsername(therapist.getUsername());
        String token = jwtService.generateToken(userDetails, therapist.getRole());

        return new AuthResponse(token, therapist.getUsername(), therapist.getRole(), therapist.getFullName());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kullanıcı bulunamadı."));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails, user.getRole());

        return new AuthResponse(token, user.getUsername(), user.getRole(), user.getFullName());
    }
}
