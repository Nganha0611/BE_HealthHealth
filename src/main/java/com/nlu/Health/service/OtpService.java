package com.nlu.Health.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {
    private final Map<String, String> otpStorage = new HashMap<>();

    @Autowired
    private EmailService mailService;

    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        return otp;
    }

    public void sendOtp(String email) throws Exception {
        String otp = generateOtp(email);
        String subject = "Xác nhận đăng ký tài khoản - Ứng dụng HealthHealth";
        String body = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);'>"
                + "<h2 style='color: #4D2D7D; text-align: center;'>Xác nhận đăng ký tài khoản</h2>"
                + "<p>Chào bạn,</p>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản trên <b>HealthHealth</b>, ứng dụng chăm sóc và theo dõi sức khỏe dành cho người cao tuổi.</p>"
                + "<p>Mã xác thực (OTP) của bạn là:</p>"
                + "<h2 style='text-align: center; color: #ff5733;'>" + otp + "</h2>"
                + "<p>Mã này có hiệu lực trong <b>5 phút</b>. Vui lòng nhập mã để hoàn tất quá trình đăng ký.</p>"
                + "<p>Nếu bạn không yêu cầu đăng ký tài khoản, vui lòng bỏ qua email này.</p>"
                + "<p>Trân trọng,</p>"
                + "<p><b>Đội ngũ HealthHealth</b></p>"
                + "</div>"
                + "</div>";
        mailService.sendEmail(email, subject, body);
    }

    public boolean verifyOtp(String email, String inputOtp) {
        return otpStorage.containsKey(email) && otpStorage.get(email).equals(inputOtp);
    }
}


