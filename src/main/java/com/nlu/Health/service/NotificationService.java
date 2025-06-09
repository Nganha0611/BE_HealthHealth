package com.nlu.Health.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nlu.Health.model.TrackingPermission;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.NotificationRepository;
import com.nlu.Health.repository.TrackingPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private TrackingPermissionRepository trackingPermissionRepository;
    @Autowired
    private NotificationRepository notificationRepository;



    public void sendNotificationToFollowers(String userId, String title, String body, com.nlu.Health.model.Notification notification) {
        User user = authRepository.findById(userId).orElse(null);

        List<TrackingPermission> followers = trackingPermissionRepository.findByFollowedUserIdAndStatus(userId, "approved");
        for (TrackingPermission permission : followers) {
            String followerId = permission.getFollowerUserId();
            User follower = authRepository.findById(followerId).orElse(null);
            if (follower != null && follower.getFcmToken() != null) {
                Message followerMessage = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setToken(follower.getFcmToken())
                        .build();
                try {
                    notificationRepository.save(notification);
                    FirebaseMessaging.getInstance().send(followerMessage);
                    logger.info("Notification sent to follower: {}", followerId);
                } catch (Exception e) {
                    logger.error("Failed to send notification to follower {}: {}", followerId, e.getMessage(), e);
                }
            }
        }
    }

    // Hàm mới: Gửi thông báo cho chính user hiện tại
    public void sendNotificationToUser(String userId, String title, String body, com.nlu.Health.model.Notification notification) {
        // Lấy thông tin người dùng
        User user = authRepository.findById(userId).orElse(null);

        // Tạo và gửi thông báo cho user
        Message userMessage = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setToken(user.getFcmToken())
                .build();

        try {
            notificationRepository.save(notification);
            FirebaseMessaging.getInstance().send(userMessage);
            logger.info("Notification sent to user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", userId, e.getMessage(), e);
        }
    }
}