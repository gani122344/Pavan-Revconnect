package org.revature.revconnect.controller;

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
import org.revature.revconnect.dto.response.ApiResponse;
import org.revature.revconnect.dto.response.AuthResponse;
import org.revature.revconnect.dto.response.TempAuthResponse;
import org.revature.revconnect.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new account with email, username, and password")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        log.info("User registered successfully: {}", response.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("User registered successfully", response),
                HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user with email/username and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        log.info("Login successful for user: {}", response.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify Email", description = "Verify email using OTP from email")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Verify email request received for: {}", request.getEmail());
        AuthResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You are now logged in.", response));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend Verification Email", description = "Resend a new 6-digit OTP to the user's email")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification config received for email: {}", request.getEmail());
        authService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success("Verification email resent. Check your inbox.", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request a password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent. Check your inbox.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request received");
        authService.resetPassword(request);
        return ResponseEntity
                .ok(ApiResponse.success("Password reset successful. You can now login with your new password.", null));
    }

    @PostMapping("/google")
    @Operation(summary = "Google OAuth login", description = "Authenticate with Google ID token")
    public ResponseEntity<ApiResponse<TempAuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        log.info("Google auth request received");
        TempAuthResponse response = authService.authenticateGoogle(request);
        String msg = response.isRequiresPhone() ? "Phone verification required" : "Login successful";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PostMapping("/twitter")
    @Operation(summary = "Twitter OAuth login", description = "Authenticate with Twitter OAuth 2.0 code")
    public ResponseEntity<ApiResponse<TempAuthResponse>> twitterAuth(@Valid @RequestBody TwitterAuthRequest request) {
        log.info("Twitter auth request received");
        TempAuthResponse response = authService.authenticateTwitter(request);
        String msg = response.isRequiresPhone() ? "Phone verification required" : "Login successful";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP", description = "Send OTP to phone number for verification")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("Send OTP request received");
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and login", description = "Verify phone OTP and complete login/registration")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Verify OTP request received");
        AuthResponse response = authService.verifyOtpAndLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }
}
