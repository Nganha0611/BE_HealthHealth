package com.nlu.Health.controller;

import com.nlu.Health.model.HeartRate;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.HeartRateRepository;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/heart-rates")
public class HeartRateController {

    @Autowired
    private HeartRateRepository heartRateRepo;

    @Autowired
    private AuthRepository authRepository;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("HeartRateController - Authorization Header: " + token); // Logging để debug

        if (token == null) {
            System.out.println("HeartRateController - Token is null");
            return null;
        }

        // Xử lý token linh hoạt: bỏ prefix "Bearer " nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        System.out.println("HeartRateController - Email from token: " + email);

        if (email == null) {
            System.out.println("HeartRateController - Invalid token");
            return null;
        }

        User user = authRepository.findByEmail(email);
        System.out.println("HeartRateController - User ID: " + (user != null ? user.getId() : "null"));

        return user != null ? user.getId() : null;
    }

    @PostMapping("/measure")
    public ResponseEntity<HeartRate> createHeartRate(@RequestBody HeartRate heartRate, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            System.out.println("HeartRateController - Unauthorized: Invalid token or user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        heartRate.setUserId(userId);
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
        heartRate.setCreatedAt(Date.from(currentTime.toInstant()));
        HeartRate savedHeartRate = heartRateRepo.save(heartRate);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedHeartRate);
    }

    @GetMapping("/measure/latest")
    public ResponseEntity<HeartRate> getLatestHeartRate(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        HeartRate latest = heartRateRepo.findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (latest == null) {
            System.out.println("HeartRateController - No heart rate data found for userId: " + userId);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(latest);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HeartRate>> getHeartRateByUser(@PathVariable String userId, HttpServletRequest request) {
        String authenticatedUserId = getUserIdFromRequest(request);
        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<HeartRate> heartRates = heartRateRepo.findByUserIdOrderByCreatedAtAsc(userId);
        return ResponseEntity.ok(heartRates);
    }
}