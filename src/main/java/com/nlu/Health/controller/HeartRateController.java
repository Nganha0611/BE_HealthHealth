package com.nlu.Health.controller;

import com.nlu.Health.model.HeartRate;
import com.nlu.Health.model.User;
import com.nlu.Health.model.Notification;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.HeartRateRepository;
import com.nlu.Health.repository.NotificationRepository;
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
    private AuthRepository authRepository;

    @Autowired
    private TrackingPermissionRepository trackingPermissionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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
            System.out.println("HeartRateController - Duplicate record found for userId: " + userId + ", createdAt: " + heartRate.getCreatedAt());
            return ResponseEntity.status(HttpStatus.OK).body(existingRecord.get());
        }

        HeartRate savedHeartRate = heartRateRepo.save(heartRate);

        User user = authRepository.findById(savedHeartRate.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (savedHeartRate.getHeartRate() > 100) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String formattedDate = sdf.format(savedHeartRate.getCreatedAt());

            String message = "Nhịp tim của bạn" + " là " + savedHeartRate.getHeartRate() +
                    " bpm vào " + formattedDate;

            Notification notification = new Notification(
                    userId,
                    "heart_rate_alert",
                    message,
                    LocalDateTime.now(),
                    "unread"
            );
            notificationRepository.save(notification);

            String title = "Cảnh báo nhịp tim!";
            String body = "Nhịp tim của " + user.getName() + " là " + savedHeartRate.getHeartRate() + " bpm. Hãy nghỉ ngơi.";
            notificationService.sendNotificationToFollowers(userId, title, body);

            sendFcmNotification(userId, "Cảnh báo nhịp tim cá nhân",
                    "Nhịp tim của bạn là " + savedHeartRate.getHeartRate() + " bpm. Muốn gọi người thân?",
                    "voice_call_prompt");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedHeartRate);
    }

    private void sendFcmNotification(String userId, String title, String body, String action) {
        try {
            String fcmToken = getFcmTokenForUser(userId);
            if (fcmToken == null || fcmToken.isEmpty()) {
                System.err.println("FCM Token is null or empty for userId: " + userId);
                return;
            }

            Message message = Message.builder()
                    .putData("title", title)
                    .putData("body", body)
                    .putData("action", action)
                    .setToken(fcmToken)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent FCM message: " + response);
        } catch (Exception e) {
            System.err.println("Failed to send FCM notification for userId " + userId + ": " + e.getMessage());
        }
    }

    private String getFcmTokenForUser(String userId) {
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFcmToken();
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