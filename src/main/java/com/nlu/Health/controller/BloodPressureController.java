package com.nlu.Health.controller;

import com.nlu.Health.model.BloodPressure;
import com.nlu.Health.model.HeartRate;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.BloodPressureRepository;
import com.nlu.Health.repository.HeartRateRepository;
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
@RequestMapping("/api/blood-pressures")
public class BloodPressureController {

    @Autowired
    private BloodPressureRepository bloodPressRepo;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private BloodPressureRepository bloodPressureRepository;

    @PostMapping("/measure")
    public ResponseEntity<BloodPressure> createBloodPressure(@RequestBody BloodPressure bloodPressure) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 7); // Thêm 7 giờ
        Date currentDate = calendar.getTime();

        bloodPressure.setCreatedAt(currentDate);
        return ResponseEntity.ok(bloodPressRepo.save(bloodPressure));
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
}
