package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.RegisterRequest;
import com.ga.equestrian.dto.response.LoginResponse;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidCodeException;
import com.ga.equestrian.exception.InvalidPasswordException;
import com.ga.equestrian.mapper.UserMapper;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.CodePurpose;
import com.ga.equestrian.model.enums.Role;
import com.ga.equestrian.model.enums.UserStatus;
import com.ga.equestrian.repository.UserRepository;

import com.ga.equestrian.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration, login and the other authentication rules.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final VerificationService verificationService;


    /**
     * Registers a new client account.
     *
     * @param registerRequest the JSON user inserted.
     * @return a UserResponse a JSON with user details.
     */
    @Transactional
    public UserResponse register(RegisterRequest registerRequest){
        String email = registerRequest.getEmail().trim().toLowerCase();
        boolean isEmailExists = userRepository.existsByEmail(email);
        if(isEmailExists){
            throw new InformationExistException("This email " + email + " already exists.");
        }
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(email);
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.CLIENT);
        User savedUser = userRepository.save(user);
        verificationService.generateAndSend(user, CodePurpose.EMAIL_VERIFICATION);
        return userMapper.toResponse(savedUser);
    }


    /**
     * Sets a new password for the account named in a valid password reset token.
     * The token is issued only after the user has entered a correct reset code.
     *
     * @param resetToken the password reset token received after verifying the reset code.
     * @param password the new password, which is hashed before it is saved.
     * @throws InvalidCodeException if the token is invalid, expired or was not issued for a password reset.
     * @throws InformationNotFoundException if no user matches the email stored in the token.
     */
    @Transactional
    public void resetPassword(String resetToken, String password){
        if(!jwtUtil.isTokenValid(resetToken, "PASSWORD_RESET")){
            throw new InvalidCodeException("Invalid or expired reset token.");
        }
        String email = jwtUtil.getEmailFromToken(resetToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        ()->new InformationNotFoundException("User with this email "+ email + " not found")
                );
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    /**
     * Logs in user after validating email, password and account status.
     *
     * @param email the user's email address.
     * @param password the user's password.
     * @return a JWT login token containing the user's email and role.
     * @throws InformationNotFoundException if the email doesn't exit or the password is wrong.
     * @throws InvalidCodeException if the user's email is not verified.
     */
    public LoginResponse login(String email, String password) {
        String userEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        ()->new InformationNotFoundException("User with this email " + email + " not found")
                );

        if(user.getUserStatus() != UserStatus.ACTIVE){
            throw new InvalidCodeException("please verify your email before login");
        }
        if(!passwordEncoder.matches(password,  user.getPassword())){
            throw new InformationNotFoundException("Invalid password or email.");
        }

        String token =  jwtUtil.generateLoginToken(user.getEmail(), user.getRole());
        return new LoginResponse(
                user.getFirstName(),
                user.getLastName(),
                token
        );
    }

    /**
     * Changes the password of the signed-in user after checking the current password.
     *
     * @param email the email of the signed-in user, taken from the JWT.
     * @param currentPassword the user's current password.
     * @param newPassword the new password.
     * @throws InformationNotFoundException if the user does not exist.
     * @throws InvalidPasswordException if the current password is wrong or the new one is the same.
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new InformationNotFoundException("User with email " + email + " not found.")
        );
        if(!passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new InvalidPasswordException("Current password is incorrect.");
        }
        if(passwordEncoder.matches(newPassword, user.getPassword())){
            throw new InvalidPasswordException("New password must be different from the current password.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Password changed for user {}", user.getId());
    }
}
