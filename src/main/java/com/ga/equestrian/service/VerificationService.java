package com.ga.equestrian.service;

import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidCodeException;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.entity.VerificationCode;
import com.ga.equestrian.model.enums.CodePurpose;
import com.ga.equestrian.model.enums.UserStatus;
import com.ga.equestrian.repository.UserRepository;
import com.ga.equestrian.repository.VerificationCodeRepository;
import com.ga.equestrian.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationService {

    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 5;
    private static final String INVALID_CODE_MESSAGE = "The code is invalid or expired";

    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    /**
     * creates a new one-time code for the user, replaces any earlier code for the same purpose.
     *
     * @param user the user receiving the code.
     * @param purpose the reason the code is issued.
     */
    @Transactional
    public void generateAndSend(User user, CodePurpose purpose){

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        VerificationCode verificationCode = verificationCodeRepository
                .findByUserAndPurpose(user, purpose)
                .orElseGet(VerificationCode::new);
        verificationCode.setUser(user);
        verificationCode.setPurpose(purpose);
        verificationCode.setCodeHash(passwordEncoder.encode(code));
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCode.setAttempts(0);
        verificationCode.setSendAt(LocalDateTime.now());
        verificationCodeRepository.save(verificationCode);
        if(purpose == CodePurpose.EMAIL_VERIFICATION){
            emailService.sendVerificationCode(user.getEmail(), code);
        }else{
            emailService.sendPasswordResetCode(user.getEmail(), code);
        }
        log.info("Issued {} code for user {}", purpose, user.getId());
    }

    /**
     * It checks the submitted code, counts wrong attempts, and deletes the code when it is correct.
     *
     * @param user the user responding the code.
     * @param purpose the reason of the code issued.
     * @param code the code inserted by the user.
     */
    @Transactional(noRollbackFor = InvalidCodeException.class)
    public void consumeCode(User user, CodePurpose purpose, String code){
        VerificationCode verificationCode = verificationCodeRepository.findByUserAndPurpose(user, purpose)
                .orElseThrow(() -> new InvalidCodeException(INVALID_CODE_MESSAGE));
        if(verificationCode.getExpiresAt().isBefore(LocalDateTime.now()) || verificationCode.getAttempts()>=MAX_ATTEMPTS){
            verificationCodeRepository.delete(verificationCode);
            throw new InvalidCodeException(INVALID_CODE_MESSAGE);
        }

        if(!passwordEncoder.matches(code, verificationCode.getCodeHash())){
            verificationCode.setAttempts(verificationCode.getAttempts()+1);
            log.warn("Wrong {} code entered for user {}", purpose, user.getId());
            throw new InvalidCodeException(INVALID_CODE_MESSAGE);
        }

        //a code only used once, it is removed after a successful check.
        verificationCodeRepository.delete(verificationCode);
    }

    /**
     * Activates a pending account when submitted verification code is correct.
     *
     * @param email the email address of the account being verified
     * @param code the verification code entered by the user.
     * @throws InvalidCodeException if the email is unknown, the account is not pending,
     *                              or the code is wrong, expired, or locked.
     */
    @Transactional(noRollbackFor = InvalidCodeException.class)
    public void verifyEmail(String email, String code){
        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElseThrow(
                () -> new InvalidCodeException(INVALID_CODE_MESSAGE)
        );
        if(user.getUserStatus()!=UserStatus.PENDING){
            throw new InvalidCodeException(INVALID_CODE_MESSAGE);
        }
        consumeCode(user, CodePurpose.EMAIL_VERIFICATION, code);
        user.setUserStatus(UserStatus.ACTIVE);
        log.info("Email verified for user {}", user.getId());

    }

    /**
     * Sends a password reset code to the user's email address.
     *
     * @param email the email address requesting a password reset.
     */
    @Transactional
    public void sendPasswordResetCode(String email) {
        String userEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new InformationNotFoundException("User with this email " + userEmail + " not found."));

        generateAndSend(user, CodePurpose.PASSWORD_RESET);
    }

    /**
     *
     * @param email
     * @param code
     * @return
     */
    @Transactional
    public String verifyResetCode(String email, String code){
        String userEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new InformationNotFoundException("User with this email " + userEmail + " not found."));

        consumeCode(user, CodePurpose.PASSWORD_RESET, code);

        return jwtUtil.generatePasswordResetToken(user.getEmail());
    }



}
