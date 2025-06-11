package com.nlu.Health.controller;

import com.nlu.Health.model.HeartRate;
import com.nlu.Health.model.User;
import com.nlu.Health.model.Notification;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.HeartRateRepository;
import com.nlu.Health.repository.NotificationRepository;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.service.NotificationService;
import com.nlu.Health.repository.TrackingPermissionRepository;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.text.SimpleDateFormat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

@RestController
@RequestMapping("/api/heart-rates")
public class HeartRateController {

    @Autowired
    private HeartRateRepository heartRateRepo;

    @Autowired
    private AuthService authService;

    @Autowired
    private NotificationService notificationService;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("HeartRateController - Authorization Header: " + token);

        if (token == null) {
            System.out.println("HeartRateController - Token is null");
            return null;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        System.out.println("HeartRateController - Email from token: " + email);

        if (email == null) {
            System.out.println("HeartRateController - Invalid token");
            return null;
        }

        User user = authService.getUsersByEmail(email);
        System.out.println("HeartRateController - User ID: " + (user != null ? user.getId() : "null"));

        return user != null ? user.getId() : null;
    }

    @PostMapping("/measure")
    public ResponseEntity<HeartRate> createHeartRate(@RequestBody HeartRate heartRate, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        heartRate.setUserId(userId);

        if (heartRate.getCreatedAt() == null) {
            ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
            heartRate.setCreatedAt(Date.from(currentTime.toInstant()));
        }

        Optional<HeartRate> existingRecord = heartRateRepo.findByUserIdAndCreatedAtAndHeartRate(
                userId,
                heartRate.getCreatedAt(),
                heartRate.getHeartRate()
        );

        if (existingRecord.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(existingRecord.get());
        }

        HeartRate savedHeartRate = heartRateRepo.save(heartRate);

        User user = authService.findUserById(userId);

        if (savedHeartRate.getHeartRate() > 100 || savedHeartRate.getHeartRate() < 50 ) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String formattedDate = sdf.format(savedHeartRate.getCreatedAt());
            String title = "Cảnh báo nhịp tim!";
            String bodyFollowers = "Nhịp tim của " + user.getName() + " là " + savedHeartRate.getHeartRate() + " bpm.";
            String bodyUser = "Nhịp tim của bạn" + " là " + savedHeartRate.getHeartRate() +
                    " bpm vào " + formattedDate;

            Notification notificationForUser = new Notification(
                    userId,
                    "heart_rate_alert",
                    bodyUser,
                    LocalDateTime.now(),
                    "unread"
            );
            Notification notificationforFollowers = new Notification(
                    userId,
                    "heart_rate_alert",
                    bodyUser,
                    LocalDateTime.now(),
                    "unread"
            );

            notificationService.sendNotificationToUser(userId, title, bodyUser, notificationForUser);
            notificationService.sendNotificationToFollowers(userId, title, bodyFollowers, notificationforFollowers);

        }
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
