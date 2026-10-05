package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.*;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.service.AuthService;
import com.ga.equestrian.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Paths related to registration, login and security.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final VerificationService verificationService;

    /**
     * Registers a new client.
     * Return 201 on success, 400 if data is invalid.
     * and 409 if the email is already registered.
     *
     * @param registerRequest the registration details sent by the client.
     * @return the created user.
     */
    @PostMapping("/users/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        UserResponse userResponse = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     *
     * @return
     */
    @PostMapping("/users/verify")
    public ResponseEntity<String> verify(@Valid @RequestBody VerifyEmailRequest verifyEmailRequest){
        verificationService.verifyEmail(verifyEmailRequest.getEmail(), verifyEmailRequest.getCode());
        return ResponseEntity.ok("Email verified successfully.");
    }

    /**
     *
     * @param forgotPasswordRequest
     * @return
     */
    @PostMapping("/users/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest){
        verificationService.sendPasswordResetCode(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok("Password reset code sent successfully.");
    }

    @PostMapping("/users/verify-reset-code")
    public ResponseEntity<String> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest verifyResetCodeRequest){
        String resetToken = verificationService.verifyResetCode(verifyResetCodeRequest.getEmail(), verifyResetCodeRequest.getCode());
        return ResponseEntity.ok(resetToken);
    }

    @PostMapping("/users/reset-password")
    public ResponseEntity<String> resetPassword(@RequestHeader("Authorization") String authorization, @Valid @RequestBody ResetPasswordRequest request){
        String resetToken = authorization.replace("Bearer ", "");

        authService.resetPassword(resetToken, request.getPassword());

        return ResponseEntity.ok("Password reset successfully");
    }

}
