package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.*;
import com.example.smart_bin_server.model.User;
import com.example.smart_bin_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final EmailService emailService;

    private static final long VERIFICATION_TOKEN_EXPIRY = 24 * 60 * 60 * 1000; // 24 hours

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();

    public UserService(UserRepository userRepository, KeycloakService keycloakService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.keycloakService = keycloakService;
        this.emailService = emailService;
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
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
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        if (System.currentTimeMillis() > user.getVerificationTokenExpiry()) {
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

        // Get tokens from Keycloak
        TokenResponse tokens = keycloakService.login(request);

        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                tokens.tokenType(),
                toDto(user)
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // Refresh token through Keycloak
        TokenResponse tokens = keycloakService.refreshAccessToken(refreshToken);

        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                tokens.tokenType(),
                null // You can decode JWT to get userId and fetch user if needed
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            keycloakService.logout(refreshToken);
        }
    }

    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password by attempting login
        try {
            LoginRequest loginRequest = new LoginRequest(user.getEmail(), oldPassword);
            keycloakService.login(loginRequest);
        } catch (Exception e) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password in Keycloak
        keycloakService.updatePassword(userId, newPassword);

        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
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

    @Transactional
    public String resetPassword(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if(!user.isEmailVerified()){
            throw new RuntimeException("Email not verified. Please verify your email first.");
        }

        String newPassword = generateRandomPassword();

        keycloakService.updatePassword(user.getId(), newPassword);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName(),
                newPassword
        );

        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        return "Password reset successful. Please check your email for the new password.";

    }

    private String generateRandomPassword(){
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        // Đảm bảo có ít nhất 1 chữ hoa
        password.append("ABCDEFGHJKLMNPQRSTUVWXYZ".charAt(random.nextInt(25)));

        // Đảm bảo có ít nhất 1 chữ thường
        password.append("abcdefghijkmnpqrstuvwxyz".charAt(random.nextInt(25)));

        // Đảm bảo có ít nhất 1 số
        password.append("23456789".charAt(random.nextInt(8)));

        // Đảm bảo có ít nhất 1 ký tự đặc biệt
        password.append("!@#$%".charAt(random.nextInt(5)));

        // Fill phần còn lại
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        // Shuffle các ký tự
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
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