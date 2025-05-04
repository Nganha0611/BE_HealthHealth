package com.nlu.Health.controller;

import com.nlu.Health.model.BloodPressure;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.BloodPressureRepository;
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
@RequestMapping("/api/blood-pressures")
public class BloodPressureController {

    @Autowired
    private BloodPressureRepository bloodPressureRepository;
    @Autowired
    private AuthRepository authRepository;

    @PostMapping("/measure")
    public ResponseEntity<BloodPressure> createBloodPressure(@RequestBody BloodPressure bloodPressure) {
        // Lấy thời gian hiện tại ở UTC
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
        bloodPressure.setCreatedAt(Date.from(currentTime.toInstant()));

        return ResponseEntity.ok(bloodPressureRepository.save(bloodPressure));
    }

    @GetMapping("/measure/latest")
    public ResponseEntity<BloodPressure> getLatestBloodPressure(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token); // Trả về email (hoặc "admin")
        User user = authRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("User ID thực tế: " + user.getId());  // Debug check

        BloodPressure latest = bloodPressureRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId());

        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public List<BloodPressure> getBloodPressureByUser(@PathVariable String userId) {
        return bloodPressureRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
}