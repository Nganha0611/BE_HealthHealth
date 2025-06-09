package com.nlu.Health.controller;

import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.model.OtpData;
import com.nlu.Health.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    @Autowired
    private OtpService otpService;
    @Autowired
    private AuthRepository authRepository;

    @PostMapping("/sendFP")
    public ResponseEntity<Map<String, String>> sendOtpFP(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();

        User existingUser = authRepository.findByEmail(email);
        if (existingUser == null) {
            response.put("result", "emailExist");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            otpService.sendOtpFP(email);
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("result", "error");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();

        User existingUser = authRepository.findByEmail(email);
        if (existingUser != null) {
            response.put("result", "emailExist");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            otpService.sendOtp(email);
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("result", "error");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        Map<String, String> response = new HashMap<>();

        // Lấy OTP từ service
        OtpData storedOtp = otpService.getOtpData(email);
        if (storedOtp == null) {
            response.put("result", "OTPnotExist");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra thời gian hết hạn
        long currentTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();
        if (currentTime > storedOtp.getExpiresAt()) {
            response.put("result", "OTPExpired");
            return ResponseEntity.badRequest().body(response);
        }

        // Xác thực OTP
        if (otpService.verifyOtp(email, otp)) {
            response.put("result", "success");
            return ResponseEntity.ok(response);
        }

        response.put("result", "error");
        return ResponseEntity.badRequest().body(response);
    }
}