package com.nlu.Health.controller;

import com.nlu.Health.model.Notification;
import com.nlu.Health.repository.NotificationRepository;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.tools.JwtUtil;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthService authService;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            System.out.println("NotificationController - Token is null");
            return null;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        if (email == null) {
            System.out.println("NotificationController - Invalid token");
            return null;
        }

        User user = authService.getUsersByEmail(email);
        return user != null ? user.getId() : null;
    }

    // Lấy tất cả thông báo của người dùng
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(notifications);
    }

    // Lấy thông báo chưa đọc của người dùng
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByTimestampDesc(userId, "unread");
        return ResponseEntity.ok(notifications);
    }

    // Thêm thông báo mới
    @PostMapping
    public ResponseEntity<Notification> createNotification(HttpServletRequest request, @RequestBody Notification notification) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (notification.getUserId() == null || notification.getType() == null ||
                notification.getMessage() == null || notification.getStatus() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (notification.getTimestamp() == null) {
            notification.setTimestamp(LocalDateTime.now());
        }

        Notification savedNotification = notificationRepository.save(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNotification);
    }

    // Cập nhật trạng thái thông báo (đã đọc)
    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotificationStatus(HttpServletRequest request, @PathVariable String id, @RequestBody Notification notificationDetails) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (!optionalNotification.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Notification notification = optionalNotification.get();
        notification.setStatus(notificationDetails.getStatus());
        Notification updatedNotification = notificationRepository.save(notification);
        return ResponseEntity.ok(updatedNotification);
    }
}