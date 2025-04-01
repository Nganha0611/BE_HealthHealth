package com.nlu.Health.controller;

import com.nlu.Health.model.User;
import com.nlu.Health.repository.UserRepository;
import com.nlu.Health.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    @Autowired
    private OtpService otpService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sendFP")
    public ResponseEntity<Map<String, String>> sendOtpFP(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();

        List<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            response.put("result", "error");
            response.put("message", "Email không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            otpService.sendOtpFP(email);
            response.put("result", "success");
            response.put("message", "OTP đã gửi đến email " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("result", "error");
            response.put("message", "Lỗi khi gửi OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();

        List<User> existingUser = userRepository.findByEmail(email);
        if (!existingUser.isEmpty()) {
            response.put("result", "error");
            response.put("message", "Email đã tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            otpService.sendOtp(email);
            response.put("result", "success");
            response.put("message", "OTP đã gửi đến email " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("result", "error");
            response.put("message", "Lỗi khi gửi OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.verifyOtp(email, otp);
        return isValid ? ResponseEntity.ok("OTP hợp lệ!") : ResponseEntity.badRequest().body("OTP không đúng!");
    }
}

