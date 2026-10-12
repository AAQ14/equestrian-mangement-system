package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.*;
import com.ga.equestrian.dto.response.LoginResponse;
import com.ga.equestrian.dto.response.MessageResponse;
import com.ga.equestrian.dto.response.ResetPasswordTokenResponse;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.service.AuthService;
import com.ga.equestrian.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
     * Verifies a new account's email using the code sent to the user.
     * Returns 200 on success and 400 if the data is invalid or the code is wrong, expired or locked.
     *
     * @param verifyEmailRequest the email and the verification code.
     * @return a confirmation message.
     */
    @PostMapping("/users/verify")
    public ResponseEntity<MessageResponse> verify(@Valid @RequestBody VerifyEmailRequest verifyEmailRequest){
        verificationService.verifyEmail(verifyEmailRequest.getEmail(), verifyEmailRequest.getCode());
        return ResponseEntity.ok(new MessageResponse("Email verified successfully."));
    }

    /**
     * Sends a password reset code to the user's email.
     * Returns 200 on success, 400 if the email is invalid, and 404 if no user has this email.
     *
     * @param forgotPasswordRequest the email of the account to recover.
     * @return a confirmation message.
     */
    @PostMapping("/users/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest){
        verificationService.sendPasswordResetCode(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok(new MessageResponse("Password reset code sent successfully."));
    }

    /**
     * Checks the password reset code and returns a short-lived reset token.
     * Returns 200 on success and 400 if the data is invalid or the code is wrong, expired or locked.
     *
     * @param verifyResetCodeRequest the email and the reset code.
     * @return the reset token to use in the reset-password request.
     */
    @PostMapping("/users/verify-reset-code")
    public ResponseEntity<ResetPasswordTokenResponse> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest verifyResetCodeRequest){
        ResetPasswordTokenResponse resetToken = verificationService.verifyResetCode(verifyResetCodeRequest.getEmail(), verifyResetCodeRequest.getCode());
        return ResponseEntity.ok().body(resetToken);
    }

    /**
     * Sets a new password using the reset token from the Authorization header.
     * Returns 200 on success and 400 if the password is invalid or the token is invalid or expired.
     *
     * @param authorization the Authorization header holding the reset token as "Bearer token".
     * @param request the new password.
     * @return a confirmation message.
     */
    @PostMapping("/users/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestHeader("Authorization") String authorization, @Valid @RequestBody ResetPasswordRequest request){
        String resetToken = authorization.replace("Bearer ", "");

        authService.resetPassword(resetToken, request.getPassword());

        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    /**
     * Logs in user and returns their name and JWT authentication token.
     *
     * @param request the login request containing the user's email and password.
     * @return a response containing the user's name and JWT login token.
     */
    @PostMapping("/users/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        System.out.println(request.getEmail()+ request.getPassword());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode(request.getPassword()));
        LoginResponse loginResponse = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    /**
     * Changes the password of the signed-in user.
     * Returns 200 on success, 400 if the data is invalid or the current password is wrong,
     * and 401 or 403 if the token is missing or invalid.
     *
     * @param authentication the signed-in user, set by the JWT filter.
     * @param request the current and the new password.
     * @return a confirmation message.
     */
    @PutMapping("/users/change-password")
    public ResponseEntity<MessageResponse> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request){
        authService.changePassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Password changed successfully."));
    }

}
