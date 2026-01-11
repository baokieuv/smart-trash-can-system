package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.AuthResponse;
import com.example.smart_bin_server.dto.LoginRequest;
import com.example.smart_bin_server.dto.RegisterRequest;
import com.example.smart_bin_server.dto.UserDto;
import com.example.smart_bin_server.model.User;
import com.example.smart_bin_server.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serial;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final EmailService emailService;

    private static final long VERIFICATION_TOKEN_EXPIRY = 24 * 60 * 60 * 1000;

    public UserDto register(RegisterRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new RuntimeException("Email already registered");
        }

        String keycloakUserId = keycloakService.createUser(request);

        User user = new User();
        user.setId(keycloakUserId);
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(System.currentTimeMillis() + VERIFICATION_TOKEN_EXPIRY);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getVerificationToken()
        );

        return toDto(savedUser);
    }

    @Transactional
    public String verifyEmail(String token){
        User user = userRepository.findByVerificationToken(token).orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if(user.isEmailVerified()){
            throw new RuntimeException("Email already verified");
        }

        if(System.currentTimeMillis() > user.getVerificationTokenExpiry()){
            throw new RuntimeException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user.setUpdatedAt(System.currentTimeMillis());

        userRepository.save(user);

        keycloakService.enableUser(user.getId());

        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        return "Email verified successfully";
    }


    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified. Please check your email for verification link.");
        }

        Map<String, Object> tokens = keycloakService.login(request);

        return new AuthResponse(
                (String) tokens.get("access_token"),
                (String) tokens.get("refresh_token"),
                (Long) tokens.get("expires_in"),
                "Bearer",
                toDto(user)
        );
    }

    public UserDto getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(user);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(System.currentTimeMillis() + VERIFICATION_TOKEN_EXPIRY);
        user.setUpdatedAt(System.currentTimeMillis());

        userRepository.save(user);

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFirstName(),
                user.getVerificationToken()
        );
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }

}
