package org.revature.revconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.exception.BadRequestException;
import org.revature.revconnect.model.PhoneOtp;
import org.revature.revconnect.repository.PhoneOtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final PhoneOtpRepository phoneOtpRepository;
    private final SmsService smsService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 30;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendOtp(String phone) {
        log.info("Sending OTP to phone: {}", maskPhone(phone));

        Optional<PhoneOtp> existing = phoneOtpRepository.findTopByPhoneOrderByCreatedAtDesc(phone);
        if (existing.isPresent()) {
            PhoneOtp existingOtp = existing.get();
            if (!existingOtp.canResend()) {
                throw new BadRequestException("Please wait 30 seconds before requesting a new OTP");
            }
            if (existingOtp.hasExceededAttempts()) {
                throw new BadRequestException("Too many OTP attempts. Please try again later.");
            }
        }

        phoneOtpRepository.deleteByPhone(phone);

        String otp = generateOtp();

        PhoneOtp phoneOtp = PhoneOtp.builder()
                .phone(phone)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .lastSentAt(LocalDateTime.now())
                .attempts(0)
                .build();

        phoneOtpRepository.save(phoneOtp);

        smsService.sendOtp(phone, otp);
        log.info("OTP sent successfully to: {}", maskPhone(phone));
    }

    @Transactional
    public boolean verifyOtp(String phone, String otp) {
        log.info("Verifying OTP for phone: {}", maskPhone(phone));

        PhoneOtp phoneOtp = phoneOtpRepository.findTopByPhoneOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new BadRequestException("No OTP found for this phone number. Please request a new one."));

        if (phoneOtp.hasExceededAttempts()) {
            phoneOtpRepository.deleteByPhone(phone);
            throw new BadRequestException("Too many failed attempts. Please request a new OTP.");
        }

        if (phoneOtp.isExpired()) {
            phoneOtpRepository.deleteByPhone(phone);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        phoneOtp.setAttempts(phoneOtp.getAttempts() + 1);
        phoneOtpRepository.save(phoneOtp);

        if (!phoneOtp.getOtp().equals(otp)) {
            int remaining = MAX_ATTEMPTS - phoneOtp.getAttempts();
            throw new BadRequestException("Invalid OTP. " + remaining + " attempts remaining.");
        }

        phoneOtpRepository.deleteByPhone(phone);
        log.info("OTP verified successfully for: {}", maskPhone(phone));
        return true;
    }

    private String generateOtp() {
        int otpNum = 100000 + random.nextInt(900000);
        return String.valueOf(otpNum);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return "****" + phone.substring(phone.length() - 4);
    }
}
