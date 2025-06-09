package com.nlu.Health.service;

import com.nlu.Health.model.OtpData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {


    private final Map<String, OtpData> otpStorage = new HashMap<>();
    private static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 phút
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    @Autowired
    private EmailService mailService;

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendOtpFP(String email) throws Exception {
        String otp = generateOtp();
        long currentTime = ZonedDateTime.now(ZONE_ID).toInstant().toEpochMilli();
        long expiresAt = currentTime + OTP_VALID_DURATION;

        otpStorage.put(email, new OtpData(otp, expiresAt));

        System.out.println("OTP created at: " + Instant.ofEpochMilli(currentTime).atZone(ZONE_ID));
        System.out.println("OTP expires at: " + Instant.ofEpochMilli(expiresAt).atZone(ZONE_ID));

        String subject = "Xác nhận đặt lại mật khẩu - Ứng dụng HealthHealth";
        String body = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);'>"
                + "<h2 style='color: #4D2D7D; text-align: center;'>Xác nhận đặt lại mật khẩu</h2>"
                + "<p>Chào bạn,</p>"
                + "<p>Mã xác thực (OTP) của bạn là:</p>"
                + "<h2 style='text-align: center; color: #ff5733;'>" + otp + "</h2>"
                + "<p>Mã này có hiệu lực trong <b>5 phút</b>.</p>"
                + "<p>Trân trọng,</p>"
                + "<p><b>Đội ngũ HealthHealth</b></p>"
                + "</div>"
                + "</div>";

        mailService.sendEmail(email, subject, body);
    }

    public void sendOtp(String email) throws Exception {
        String otp = generateOtp();
        long currentTime = ZonedDateTime.now(ZONE_ID).toInstant().toEpochMilli();
        long expiresAt = currentTime + OTP_VALID_DURATION;

        otpStorage.put(email, new OtpData(otp, expiresAt));

        System.out.println("OTP created at: " + Instant.ofEpochMilli(currentTime).atZone(ZONE_ID));
        System.out.println("OTP expires at: " + Instant.ofEpochMilli(expiresAt).atZone(ZONE_ID));

        String subject = "Xác nhận đăng ký tài khoản - Ứng dụng HealthHealth";
        String body = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);'>"
                + "<h2 style='color: #4D2D7D; text-align: center;'>Xác nhận đăng ký tài khoản</h2>"
                + "<p>Chào bạn,</p>"
                + "<p>Mã xác thực (OTP) của bạn là:</p>"
                + "<h2 style='text-align: center; color: #ff5733;'>" + otp + "</h2>"
                + "<p>Mã này có hiệu lực trong <b>5 phút</b>.</p>"
                + "<p>Trân trọng,</p>"
                + "<p><b>Đội ngũ HealthHealth</b></p>"
                + "</div>"
                + "</div>";

        mailService.sendEmail(email, subject, body);
    }

    public boolean verifyOtp(String email, String otp) {
        OtpData storedOtp = otpStorage.get(email);
        long currentTime = ZonedDateTime.now(ZONE_ID).toInstant().toEpochMilli();

        if (storedOtp == null) {
            System.out.println("OTP not found for email: " + email);
            return false;
        }

        System.out.println("Current time: " + Instant.ofEpochMilli(currentTime).atZone(ZONE_ID));
        System.out.println("OTP expires at: " + Instant.ofEpochMilli(storedOtp.getExpiresAt()).atZone(ZONE_ID));

        if (currentTime > storedOtp.getExpiresAt()) {
            otpStorage.remove(email);
            System.out.println("OTP has expired for email: " + email);
            return false;
        }

        if (storedOtp.getOtp().equals(otp)) {
            otpStorage.remove(email);
            System.out.println("OTP verified successfully for email: " + email);
            return true;
        }

        System.out.println("Invalid OTP for email: " + email);
        return false;
    }

    public OtpData getOtpData(String email) {
        return otpStorage.get(email);
    }
}