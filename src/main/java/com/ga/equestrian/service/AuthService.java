package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.RegisterRequest;
import com.ga.equestrian.dto.response.LoginResponse;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidCodeException;
import com.ga.equestrian.mapper.UserMapper;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.CodePurpose;
import com.ga.equestrian.model.enums.Role;
import com.ga.equestrian.model.enums.UserStatus;
import com.ga.equestrian.repository.UserRepository;

import com.ga.equestrian.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration, login and the other authentication rules.
 */
@Service
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
     * Resets the user's password after validating the password reset code.
     **/
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
        if(!passwordEncoder.matches(user.getPassword(), password)){
            throw new InformationNotFoundException("Invalid password or email.");
        }

        String token =  jwtUtil.generateLoginToken(user.getEmail(), user.getRole());
        return new LoginResponse(
                user.getFirstName(),
                user.getLastName(),
                token
        );
    }

}
