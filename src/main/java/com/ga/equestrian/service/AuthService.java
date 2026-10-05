package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.RegisterRequest;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.mapper.UserMapper;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.CodePurpose;
import com.ga.equestrian.model.enums.Role;
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


}
