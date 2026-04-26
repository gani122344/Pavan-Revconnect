package org.revature.revconnect.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.revature.revconnect.dto.request.ForgotPasswordRequest;
import org.revature.revconnect.dto.request.GoogleAuthRequest;
import org.revature.revconnect.dto.request.LoginRequest;
import org.revature.revconnect.dto.request.RegisterRequest;
import org.revature.revconnect.dto.request.ResetPasswordRequest;
import org.revature.revconnect.dto.request.SendOtpRequest;
import org.revature.revconnect.dto.request.TwitterAuthRequest;
import org.revature.revconnect.dto.request.VerifyEmailRequest;
import org.revature.revconnect.dto.request.VerifyOtpRequest;
import org.revature.revconnect.dto.request.ResendVerificationRequest;
import org.revature.revconnect.dto.response.AuthResponse;
import org.revature.revconnect.dto.response.TempAuthResponse;
import org.revature.revconnect.enums.AuthProvider;
import org.revature.revconnect.exception.BadRequestException;
import org.revature.revconnect.exception.DuplicateResourceException;
import org.revature.revconnect.exception.ResourceNotFoundException;
import org.revature.revconnect.model.PasswordResetToken;
import org.revature.revconnect.model.User;
import org.revature.revconnect.model.UserSettings;
import org.revature.revconnect.repository.PasswordResetTokenRepository;
import org.revature.revconnect.repository.UserRepository;
import org.revature.revconnect.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpService otpService;

    @Value("${google.client.id:}")
    private String googleClientId;

    @Value("${twitter.client.id:}")
    private String twitterClientId;

    @Value("${twitter.client.secret:}")
    private String twitterClientSecret;

    @Value("${twitter.redirect.uri:}")
    private String twitterRedirectUri;

    private static final int TOKEN_EXPIRY_HOURS = 24;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .userType(request.getUserType())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Create default user settings
        UserSettings settings = UserSettings.builder()
                .user(savedUser)
                .build();
        userSettingsRepository.save(settings);

        // Generate 6-digit OTP for Email Verification
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        PasswordResetToken verificationToken = PasswordResetToken.builder()
                .token(otp)
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .build();
        passwordResetTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), otp);

        return AuthResponse.builder()
                .accessToken("") // no token until verified
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .userType(savedUser.getUserType())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException("Please verify your email before logging in.");
        }

        // Reactivate account if it was deactivated
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            user.setIsActive(true);
            userRepository.save(user);
            log.info("Account reactivated on login for user: {}", user.getUsername());
        }

        String token = jwtTokenProvider.generateToken(user);

        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .userType(user.getUserType())
                .build();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No authenticated user found");
        }
        return (User) authentication.getPrincipal();
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        log.info("Processing email verification for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getIsVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("No verification token found"));

        if (!token.getToken().equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (token.isExpired()) {
            passwordResetTokenRepository.delete(token);
            throw new BadRequestException("Verification OTP has expired. Please request a new one.");
        }

        user.setIsVerified(true);
        userRepository.save(user);
        passwordResetTokenRepository.delete(token);

        String jwt = jwtTokenProvider.generateToken(user);

        log.info("Email verification successful for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .userType(user.getUserType())
                .build();
    }

    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        log.info("Processing resend verification request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getIsVerified()) {
            throw new BadRequestException("Email is already verified. Please log in.");
        }

        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);
        passwordResetTokenRepository.flush();

        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        PasswordResetToken newToken = PasswordResetToken.builder()
                .token(otp)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .build();
        passwordResetTokenRepository.save(newToken);

        emailService.sendVerificationEmail(user.getEmail(), otp);
        log.info("Verification email resent for user: {}", user.getUsername());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        log.info("User found for email: {}", user.getUsername());

        // Generate new 6-digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        log.info("Generated 6-digit OTP: {}", otp);

        // Find existing token and delete it to match test expectation and ensure fresh
        // OTP
        passwordResetTokenRepository.findByUser(user).ifPresent(token -> {
            passwordResetTokenRepository.delete(token);
            passwordResetTokenRepository.flush(); // Ensure deletion is flushed
        });

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(otp);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));

        try {
            passwordResetTokenRepository.save(resetToken);
            log.info("Password reset token (OTP) saved successfully for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error while saving reset token: {}", e.getMessage());
            // If it still fails due to uniqueness, try one more time by deleting first and
            // flushing
            try {
                log.info("Fall-back: Deleting and flushing before save...");
                passwordResetTokenRepository.deleteByUser(user);
                passwordResetTokenRepository.flush();

                PasswordResetToken newToken = PasswordResetToken.builder()
                        .token(otp)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                        .build();
                passwordResetTokenRepository.save(newToken);
                log.info("Password reset token saved successfully after fall-back.");
            } catch (Exception ex) {
                log.error("Critical error in fall-back: {}", ex.getMessage());
                throw new BadRequestException("Failed to initiate password reset. Please try again later.");
            }
        }

        // Send email (mocked in dev)
        log.info("Calling email service to send OTP...");
        emailService.sendPasswordResetEmail(user.getEmail(), otp);
        log.info("Forgot password process completed for email: {}", request.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset for token");

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete the used token
        passwordResetTokenRepository.delete(resetToken);

        log.info("Password reset successful for user: {}", user.getUsername());
    }

    // ──────────────────────────────────────────────────
    //  GOOGLE OAUTH
    // ──────────────────────────────────────────────────
    @Transactional
    public TempAuthResponse authenticateGoogle(GoogleAuthRequest request) {
        log.info("Processing Google authentication");

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new BadRequestException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            if (email == null || email.isEmpty()) {
                throw new BadRequestException("Google account has no email");
            }

            // Check if user already exists with this email
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (user.getIsPhoneVerified() && user.getPhone() != null) {
                    // Existing verified user — generate JWT directly
                    String jwt = jwtTokenProvider.generateToken(user);
                    return TempAuthResponse.builder()
                            .tempToken(jwt)
                            .email(email)
                            .name(user.getName())
                            .provider("google")
                            .requiresPhone(false)
                            .build();
                }
            }

            // Generate temp token with user info for phone verification step
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("name", name != null ? name : "User");
            claims.put("picture", pictureUrl != null ? pictureUrl : "");
            claims.put("provider", "google");
            claims.put("type", "temp_oauth");

            // Create a temporary user details for token generation
            User tempUser = User.builder()
                    .username("temp_" + UUID.randomUUID().toString().substring(0, 8))
                    .name(name != null ? name : "User")
                    .email(email)
                    .build();

            String tempToken = jwtTokenProvider.generateToken(claims, tempUser);

            log.info("Google auth successful for email: {}", email);

            return TempAuthResponse.builder()
                    .tempToken(tempToken)
                    .email(email)
                    .name(name)
                    .provider("google")
                    .requiresPhone(true)
                    .build();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google authentication failed: {}", e.getMessage());
            throw new BadRequestException("Google authentication failed: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────
    //  TWITTER OAUTH
    // ──────────────────────────────────────────────────
    @Transactional
    @SuppressWarnings("unchecked")
    public TempAuthResponse authenticateTwitter(TwitterAuthRequest request) {
        log.info("Processing Twitter authentication");

        try {
            // Exchange authorization code for access token
            WebClient webClient = WebClient.builder().build();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("code", request.getCode());
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", twitterClientId);
            formData.add("redirect_uri", twitterRedirectUri);
            formData.add("code_verifier", request.getCodeVerifier());

            String basicAuth = Base64.getEncoder().encodeToString(
                    (twitterClientId + ":" + twitterClientSecret).getBytes());

            Map<String, Object> tokenResponse = webClient.post()
                    .uri("https://api.twitter.com/2/oauth2/token")
                    .header("Authorization", "Basic " + basicAuth)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new BadRequestException("Failed to get Twitter access token");
            }

            String accessToken = (String) tokenResponse.get("access_token");

            // Get user info from Twitter
            Map<String, Object> userResponse = webClient.get()
                    .uri("https://api.twitter.com/2/users/me?user.fields=name,username,profile_image_url")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (userResponse == null || !userResponse.containsKey("data")) {
                throw new BadRequestException("Failed to get Twitter user info");
            }

            Map<String, Object> userData = (Map<String, Object>) userResponse.get("data");
            String name = (String) userData.get("name");
            String twitterUsername = (String) userData.get("username");
            String profileImage = (String) userData.get("profile_image_url");

            // Check if user exists by twitter username mapping
            Optional<User> existingUser = userRepository.findByEmail(twitterUsername + "@twitter.oauth");
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (user.getIsPhoneVerified() && user.getPhone() != null) {
                    String jwt = jwtTokenProvider.generateToken(user);
                    return TempAuthResponse.builder()
                            .tempToken(jwt)
                            .email(twitterUsername)
                            .name(user.getName())
                            .provider("twitter")
                            .requiresPhone(false)
                            .build();
                }
            }

            // Generate temp token
            Map<String, Object> claims = new HashMap<>();
            claims.put("twitterUsername", twitterUsername);
            claims.put("name", name != null ? name : twitterUsername);
            claims.put("picture", profileImage != null ? profileImage : "");
            claims.put("provider", "twitter");
            claims.put("type", "temp_oauth");

            User tempUser = User.builder()
                    .username("temp_" + UUID.randomUUID().toString().substring(0, 8))
                    .name(name != null ? name : twitterUsername)
                    .build();

            String tempToken = jwtTokenProvider.generateToken(claims, tempUser);

            log.info("Twitter auth successful for: {}", twitterUsername);

            return TempAuthResponse.builder()
                    .tempToken(tempToken)
                    .email(twitterUsername)
                    .name(name)
                    .provider("twitter")
                    .requiresPhone(true)
                    .build();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Twitter authentication failed: {}", e.getMessage());
            throw new BadRequestException("Twitter authentication failed: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────
    //  SEND OTP
    // ──────────────────────────────────────────────────
    public void sendOtp(SendOtpRequest request) {
        log.info("Sending OTP to phone");
        otpService.sendOtp(request.getPhone());
    }

    // ──────────────────────────────────────────────────
    //  VERIFY OTP & LOGIN/REGISTER
    // ──────────────────────────────────────────────────
    @Transactional
    public AuthResponse verifyOtpAndLogin(VerifyOtpRequest request) {
        log.info("Verifying OTP and completing auth");

        // Verify OTP first
        otpService.verifyOtp(request.getPhone(), request.getOtp());

        String tempToken = request.getTempToken();
        String email = null;
        String name = null;
        String picture = null;
        AuthProvider provider = AuthProvider.PHONE;

        // Decode temp token if present (from Google/Twitter OAuth)
        if (tempToken != null && !tempToken.isEmpty()) {
            try {
                if (jwtTokenProvider.validateToken(tempToken)) {
                    var claims = jwtTokenProvider.extractAllClaimsPublic(tempToken);
                    String type = (String) claims.get("type");

                    if ("temp_oauth".equals(type)) {
                        String providerStr = (String) claims.get("provider");
                        name = (String) claims.get("name");
                        picture = (String) claims.get("picture");

                        if ("google".equals(providerStr)) {
                            email = (String) claims.get("email");
                            provider = AuthProvider.GOOGLE;
                        } else if ("twitter".equals(providerStr)) {
                            String twitterUsername = (String) claims.get("twitterUsername");
                            email = twitterUsername + "@twitter.oauth";
                            provider = AuthProvider.TWITTER;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Invalid temp token, treating as phone-only login: {}", e.getMessage());
            }
        }

        // Ensure we always have a non-null unique email for phone-only users.
        // This prevents DB constraint failures when the schema requires email.
        if (email == null || email.isBlank()) {
            String phoneDigits = request.getPhone() == null ? "" : request.getPhone().replaceAll("[^0-9]", "");
            email = "phone_" + phoneDigits + "@revconnect.local";
        }

        // Find or create user
        Optional<User> existingByPhone = userRepository.findByPhone(request.getPhone());
        Optional<User> existingByEmail = (email != null) ? userRepository.findByEmail(email) : Optional.empty();

        User user;
        if (existingByPhone.isPresent()) {
            user = existingByPhone.get();
            user.setIsPhoneVerified(true);
            if (user.getEmail() == null || user.getEmail().isBlank()) user.setEmail(email);
            if (picture != null && user.getProfilePicture() == null) {
                user.setProfilePicture(picture);
            }
        } else if (existingByEmail.isPresent()) {
            user = existingByEmail.get();
            user.setPhone(request.getPhone());
            user.setIsPhoneVerified(true);
            user.setAuthProvider(provider);
            if (picture != null && user.getProfilePicture() == null) {
                user.setProfilePicture(picture);
            }
        } else {
            // Create new user
            String userName = name;
            if (userName == null || userName.isEmpty()) {
                userName = "User" + request.getPhone().substring(Math.max(0, request.getPhone().length() - 4));
            }

            String username = generateUsername(userName);

            user = User.builder()
                    .username(username)
                    .name(userName)
                    .email(email)
                    .phone(request.getPhone())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .authProvider(provider)
                    .isPhoneVerified(true)
                    .isVerified(true)
                    .isActive(true)
                    .profilePicture(picture)
                    .build();

            user = userRepository.save(user);

            // Create default settings
            UserSettings settings = UserSettings.builder().user(user).build();
            userSettingsRepository.save(settings);

            log.info("New user created: {} ({})", username, provider);
        }

        user.setIsVerified(true);
        userRepository.save(user);

        String jwt = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .userType(user.getUserType())
                .build();
    }

    // ──────────────────────────────────────────────────
    //  USERNAME GENERATION
    // ──────────────────────────────────────────────────
    public String generateUsername(String name) {
        String base = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (base.isEmpty()) base = "user";
        if (base.length() > 20) base = base.substring(0, 20);

        Random random = new Random();
        String username;
        int attempts = 0;
        do {
            int digits = 1000 + random.nextInt(9000);
            username = base + digits;
            attempts++;
            if (attempts > 50) {
                username = base + System.currentTimeMillis() % 100000;
                break;
            }
        } while (userRepository.existsByUsername(username));

        return username;
    }
}
