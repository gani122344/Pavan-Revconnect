package org.revature.revconnect.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${sms.provider:log}")
    private String smsProvider;

    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    public void sendOtp(String phone, String otp) {
        switch (smsProvider.toLowerCase()) {
            case "twilio":
                sendViaTwilio(phone, otp);
                break;
            case "log":
            default:
                sendViaLog(phone, otp);
                break;
        }
    }

    private void sendViaLog(String phone, String otp) {
        log.info("========================================");
        log.info("  SMS OTP for {}: {}", phone, otp);
        log.info("========================================");
    }

    private void sendViaTwilio(String phone, String otp) {
        if (twilioAccountSid == null || twilioAccountSid.isBlank()
                || twilioAuthToken == null || twilioAuthToken.isBlank()
                || twilioPhoneNumber == null || twilioPhoneNumber.isBlank()) {
            log.warn("Twilio credentials not configured. Falling back to log.");
            sendViaLog(phone, otp);
            return;
        }

        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message.creator(
                    new PhoneNumber(phone),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your RevConnect verification code is: " + otp + ". Valid for 5 minutes.")
                .create();
        } catch (Exception e) {
            log.warn("Twilio SMS send failed: {}. Falling back to log.", e.getMessage());
            sendViaLog(phone, otp);
        }
    }
}
