package com.nlu.Health.controller;

import com.nlu.Health.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        try {
            otpService.sendOtp(email);
            return ResponseEntity.ok("OTP đã gửi đến email " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi OTP: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.verifyOtp(email, otp);
        return isValid ? ResponseEntity.ok("OTP hợp lệ!") : ResponseEntity.badRequest().body("OTP không đúng!");
    }
}

