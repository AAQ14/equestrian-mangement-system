package com.ga.equestrian.service;

import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.entity.VerificationCode;
import com.ga.equestrian.model.enums.CodePurpose;
import com.ga.equestrian.repository.VerificationCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationService {

    private static final int CODE_EXPIRY_MINUTES = 15;

    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

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
}
