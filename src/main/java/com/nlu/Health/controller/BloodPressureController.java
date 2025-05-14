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

    // Hàm lấy userId từ token, tương tự PrescriptionController
    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("BloodPressureController - Authorization Header: " + token); // Logging để debug

        if (token == null) {
            System.out.println("BloodPressureController - Token is null");
            return null;
        }

        // Xử lý token linh hoạt: bỏ prefix "Bearer " nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        System.out.println("BloodPressureController - Email from token: " + email);

        if (email == null) {
            System.out.println("BloodPressureController - Invalid token");
            return null;
        }

        User user = authRepository.findByEmail(email);
        System.out.println("BloodPressureController - User ID: " + (user != null ? user.getId() : "null"));

        return user != null ? user.getId() : null;
    }

    @PostMapping("/measure")
    public ResponseEntity<BloodPressure> createBloodPressure(@RequestBody BloodPressure bloodPressure, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            System.out.println("BloodPressureController - Unauthorized: Invalid token or user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        bloodPressure.setUserId(userId);
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
        bloodPressure.setCreatedAt(Date.from(currentTime.toInstant()));

        System.out.println("BloodPressureController - Creating blood pressure for userId: " + userId);
        BloodPressure savedBloodPressure = bloodPressureRepository.save(bloodPressure);
        System.out.println("BloodPressureController - Blood pressure created with ID: " + savedBloodPressure.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedBloodPressure);
    }

    @GetMapping("/measure/latest")
    public ResponseEntity<BloodPressure> getLatestBloodPressure(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            System.out.println("BloodPressureController - Unauthorized: Invalid token or user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("BloodPressureController - Fetching latest blood pressure for userId: " + userId);
        BloodPressure latest = bloodPressureRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (latest == null) {
            System.out.println("BloodPressureController - No blood pressure data found for userId: " + userId);
            return ResponseEntity.noContent().build();
        }

        System.out.println("BloodPressureController - Found latest blood pressure for userId: " + userId);
        return ResponseEntity.ok(latest);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BloodPressure>> getBloodPressureByUser(@PathVariable String userId, HttpServletRequest request) {
        String authenticatedUserId = getUserIdFromRequest(request);
        if (authenticatedUserId == null) {
            System.out.println("BloodPressureController - Unauthorized: Invalid token or user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Kiểm tra userId khớp với user từ token
        if (!authenticatedUserId.equals(userId)) {
            System.out.println("BloodPressureController - Forbidden: userId " + userId + " does not match authenticated user " + authenticatedUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        System.out.println("BloodPressureController - Fetching blood pressures for userId: " + userId);
        List<BloodPressure> bloodPressures = bloodPressureRepository.findByUserIdOrderByCreatedAtAsc(userId);

        if (bloodPressures.isEmpty()) {
            System.out.println("BloodPressureController - No blood pressure data found for userId: " + userId);
            return ResponseEntity.noContent().build();
        }

        System.out.println("BloodPressureController - Found " + bloodPressures.size() + " blood pressure records for userId: " + userId);
        return ResponseEntity.ok(bloodPressures);
    }
}