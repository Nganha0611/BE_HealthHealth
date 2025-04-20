package com.nlu.Health.controller;

import com.nlu.Health.model.HeartRate;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.HeartRateRepository;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
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
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 7); // Thêm 7 giờ
        Date currentDate = calendar.getTime();

        heartRate.setCreatedAt(currentDate);
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

        System.out.println("User ID thực tế: " + user.getId());  // ← Phải là chuỗi như trong Mongo

        HeartRate latest = heartRateRepo.findFirstByUserIdOrderByCreatedAtDesc(user.getId());

        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
    }


}
