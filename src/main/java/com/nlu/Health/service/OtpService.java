package com.nlu.Health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {
    private final Map<String, OtpData> otpStorage = new HashMap<>();
    private static final long OTP_VALID_DURATION = 5 * 60 * 1000;

    @Autowired
    private EmailService mailService;

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendOtpFP(String email) throws Exception {
        String otp = generateOtp();
        long expiresAt = System.currentTimeMillis() + OTP_VALID_DURATION;

        otpStorage.put(email, new OtpData(otp, expiresAt));

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
        long expiresAt = System.currentTimeMillis() + OTP_VALID_DURATION;

        otpStorage.put(email, new OtpData(otp, expiresAt));

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

    public boolean verifyOtp(String email, String inputOtp) {
        OtpData storedOtp = otpStorage.get(email);

        if (storedOtp == null || System.currentTimeMillis() > storedOtp.getExpiresAt()) {
            otpStorage.remove(email);
            return false; // OTP không hợp lệ hoặc đã hết hạn
        }

        if (storedOtp.getOtp().equals(inputOtp)) {
            otpStorage.remove(email); // Xóa OTP sau khi xác thực thành công
            return true;
        }

        return false;
    }

    // ✅ Định nghĩa class OtpData để lưu OTP và thời gian hết hạn
    private static class OtpData {
        private final String otp;
        private final long expiresAt;

        public OtpData(String otp, long expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpiresAt() {
            return expiresAt;
        }
    }
}
