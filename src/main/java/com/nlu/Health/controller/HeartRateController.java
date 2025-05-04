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
import java.util.TimeZone;

@RestController
@RequestMapping("/api/heart-rates")
public class HeartRateController {

    @Autowired
    private HeartRateRepository heartRateRepo;
    @Autowired
    private AuthRepository authRepository;

    @PostMapping("/measure")
    public ResponseEntity<HeartRate> createHeartRate(@RequestBody HeartRate heartRate) {
        // Lấy thời gian hiện tại ở UTC
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
        heartRate.setCreatedAt(Date.from(currentTime.toInstant()));

        return ResponseEntity.ok(heartRateRepo.save(heartRate));
    }

    @GetMapping("/measure/latest")
    public ResponseEntity<HeartRate> getLatestHeartRate(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token); // Trả ra "admin" hoặc email
        User user = authRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("User ID thực tế: " + user.getId());

        HeartRate latest = heartRateRepo.findFirstByUserIdOrderByCreatedAtDesc(user.getId());

        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public List<HeartRate> getHeartRateByUser(@PathVariable String userId) {
        return heartRateRepo.findByUserIdOrderByCreatedAtAsc(userId);
    }
}