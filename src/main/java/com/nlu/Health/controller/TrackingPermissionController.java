package com.nlu.Health.controller;

import com.nlu.Health.model.Notification;
import com.nlu.Health.model.TrackingPermission;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.*;
import com.nlu.Health.service.NotificationService;
import com.nlu.Health.service.TrackingPermissionService;
import com.nlu.Health.tools.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class TrackingPermissionController {
    private static final Logger logger = LoggerFactory.getLogger(TrackingPermissionController.class);
    @Autowired
    private TrackingPermissionService trackingPermissionService;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private HeartRateRepository heartRateRepository;
    @Autowired
    private BloodPressureRepository bloodPressureRepository;
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;
    @Autowired
    private MedicineHistoryRepository medicineHistoryRepository;
    @Autowired
    private NotificationService notificationService; // Inject NotificationService
    @Autowired
    private NotificationRepository notificationRepository;
    @CrossOrigin(origins = "*")
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestTrackingPermission(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) {
        String token = authHeader.substring(7);
        String followerEmail = JwtUtil.validateToken(token);
        if (followerEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "unauthorized", "message", "Token không hợp lệ"));
        }

        String followedEmail = payload.get("followedEmail");
//        if (followedEmail == null ) {
//            logger.warn("Bad request: followedEmail is null or empty");
//            return ResponseEntity.badRequest()
//                    .body(Map.of("result", "userNotFound", "message", "followedEmail không được để trống"));
//        }

        User followerUser = authRepository.findByEmail(followerEmail);
//        if (followerUser == null) {
//            logger.error("User not found for followerEmail: {}", followerEmail);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("result", "userNotFound", "message", "Không tìm thấy tài khoản người theo dõi"));
//        }
        String followerUserId = followerUser.getId();

        User followedUser = authRepository.findByEmail(followedEmail);
        if (followedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "userNotFound", "message", "Không tìm thấy tài khoản được theo dõi"));
        }
        String followedUserId = followedUser.getId();

        try {
            TrackingPermission permission = trackingPermissionService.createTrackingPermission(followerUserId, followedUserId);
            logger.info("Tracking permission created successfully for followerUserId: {}, followedUserId: {}", followerUserId, followedUserId);

            // Gửi thông báo cho người được theo dõi về yêu cầu mới
            String title = "Yêu cầu theo dõi sức khỏe!";
            String body = (followerUser.getName() != null ? followerUser.getName() : "Người dùng") +
                    " đã gửi yêu cầu theo dõi sức khỏe của bạn.";

                Notification notification = new Notification(
                        followedUserId,
                        "follower",
                        body,
                        LocalDateTime.now(),
                        "unread"
                );
                notificationService.sendNotificationToUser(followedUserId, title, body,notification);
                logger.info("Notification sent to followedUserId: {} with title: {}", followedUserId, title);

            return ResponseEntity.ok(Map.of("result", "success", "message", "Yêu cầu theo dõi đã được gửi", "id", permission.getId()));
        } catch (IllegalStateException e) {
            logger.error("Conflict error: {}", e.getMessage());
            if(e.getMessage().equals("pending")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("result", "pending"));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("result", "approved"));
            }

        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", "Lỗi không xác định: " + e.getMessage()));
        }
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/update-status/{id}")
    public ResponseEntity<Map<String, String>> updateTrackingPermissionStatus(
            @PathVariable String id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
//        if (!List.of("pending", "approved", "rejected", "canceled").contains(status)) {
//            logger.warn("Invalid status: {}", status);
//            return ResponseEntity.badRequest()
//                    .body(Map.of("result", "error", "message", "Trạng thái không hợp lệ"));
//        }

        TrackingPermission updatedPermission = trackingPermissionService.updateTrackingPermissionStatus(id, status);
        if (updatedPermission != null) {
            User followerUser = authRepository.findById(updatedPermission.getFollowerUserId())
                    .orElseThrow(() -> new RuntimeException("Follower user not found"));
            User followedUser = authRepository.findById(updatedPermission.getFollowedUserId())
                    .orElseThrow(() -> new RuntimeException("Followed user not found"));

            // Gửi thông báo dựa trên trạng thái
            String title = "Cập nhật yêu cầu theo dõi!";
            String body = "";
            try {
                if ("approved".equals(status)) {
                    body = followedUser.getName() + " đã chấp thuận yêu cầu theo dõi của bạn.";
                    Notification notification = new Notification(
                            followerUser.getId(),
                            "follow",
                            body,
                            LocalDateTime.now(),
                            "unread"
                    );
                    notificationService.sendNotificationToUser(followerUser.getId(), title, body, notification);
                    logger.info("Notification sent to followerUserId: {} with title: {}", followerUser.getId(), title);
                } else if ("rejected".equals(status)) {
                    body = followedUser.getName() + " đã từ chối yêu cầu theo dõi của bạn.";
                    Notification notification = new Notification(
                            followerUser.getId(),
                            "follow",
                            body,
                            LocalDateTime.now(),
                            "unread"
                    );
                    notificationService.sendNotificationToUser(followerUser.getId(), title, body, notification);
                    logger.info("Notification sent to followerUserId: {} with title: {}", followerUser.getId(), title);
                }
//                else if ("canceled".equals(status)) {
//                    body = followerUser.getName() + " đã hủy yêu cầu theo dõi.";
//                    Notification notification = new Notification(
//                            followerUser.getId(),
//                            "follow",
//                            body,
//                            LocalDateTime.now(),
//                            "unread"
//                    );
//                    notificationService.sendNotificationToUser(followerUser.getId(), title, body, notification);
//                    logger.info("Notification sent to followedUserId: {} with title: {}", followedUser.getId(), title);
//                }

            } catch (Exception e) {
                logger.error("Failed to send notification: {}", e.getMessage(), e);
            }
            logger.info("Tracking permission status updated to: {} for id: {}", status, id);
            return ResponseEntity.ok(Map.of("result", "success", "message", "Cập nhật trạng thái thành công"));
        }
        logger.warn("Tracking permission not found for id: {}", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("result", "notFound", "message", "Không tìm thấy yêu cầu theo dõi"));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/permissions")
    public ResponseEntity<?> getTrackingPermissions(
            @RequestHeader("Authorization") String authHeader, @RequestParam boolean isFollower) {
        String token = authHeader.substring(7);
        String email = JwtUtil.validateToken(token);
        if (email == null) {
            logger.error("Unauthorized: Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "unauthorized", "message", "Token không hợp lệ"));
        }

        User user = authRepository.findByEmail(email);
        if (user == null) {
            logger.error("User not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "userNotFound", "message", "Không tìm thấy tài khoản"));
        }
        String userId = user.getId();

        return ResponseEntity.ok(trackingPermissionService.getPermissionsByUserId(userId, isFollower));
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<Map<String, String>> cancelTrackingPermission(@PathVariable String id) {
        TrackingPermission permission = trackingPermissionService.findById(id);
        if (permission == null) {
            logger.warn("Tracking permission not found for id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "notFound", "message", "Không tìm thấy yêu cầu theo dõi"));
        }

//        User followerUser = authRepository.findById(permission.getFollowerUserId())
//                .orElseThrow(() -> new RuntimeException("Follower user not found"));
//        User followedUser = authRepository.findById(permission.getFollowedUserId())
//                .orElseThrow(() -> new RuntimeException("Followed user not found"));

//        String title = "Yêu cầu theo dõi bị hủy!";
//        String body = (followerUser.getName() != null ? followerUser.getName() : "Người dùng") +
//                " đã hủy yêu cầu theo dõi sức khỏe.";
//        try {
//            Notification notification = new Notification(
//                    followedUser.getId(),
//                    "follow",
//                    body,
//                    LocalDateTime.now(),
//                    "unread"
//            );
//            notificationService.sendNotificationToUser(followedUser.getId(), title, body, notification);
//            logger.info("Notification sent to followedUserId: {} with title: {}", followedUser.getId(), title);
//        } catch (Exception e) {
//            logger.error("Failed to send notification to followedUserId: {}. Error: {}", followedUser.getId(), e.getMessage(), e);
//        }

        trackingPermissionService.deleteTrackingPermission(id);
        logger.info("Tracking permission canceled for id: {}", id);
        return ResponseEntity.ok(Map.of("result", "success", "message", "Yêu cầu theo dõi đã bị hủy"));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/permissions/{followedUserId}/health-data")
    public ResponseEntity<?> getFollowedUserHealthData(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String followedUserId) {
        String token = authHeader.substring(7);
        String followerEmail = JwtUtil.validateToken(token);
        if (followerEmail == null) {
            logger.error("Unauthorized: Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "unauthorized", "message", "Token không hợp lệ"));
        }

        User followerUser = authRepository.findByEmail(followerEmail);
        if (followerUser == null) {
            logger.error("User not found for followerEmail: {}", followerEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "userNotFound"));
        }
        String followerUserId = followerUser.getId();

        // Kiểm tra quyền truy cập trong tracking_permissions
        TrackingPermission permission = trackingPermissionService.findByFollowerAndFollowed(followerUserId, followedUserId);
        if (permission == null || !permission.getStatus().equals("approved")) {
            logger.warn("Forbidden: No permission for followerUserId: {} to access followedUserId: {}", followerUserId, followedUserId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "noPermission'"));
        }

        // Lấy dữ liệu sức khỏe của followedUser
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("heart_rates", heartRateRepository.findByUserIdOrderByCreatedAtAsc(followedUserId));
        healthData.put("blood_pressures", bloodPressureRepository.findByUserIdOrderByCreatedAtAsc(followedUserId));
        healthData.put("prescriptions", prescriptionRepository.findByUserIdOrderByNameAsc(followedUserId));
        healthData.put("medical_history", medicalHistoryRepository.findByUserIdOrderByTimestampDesc(followedUserId));
        healthData.put("medicine_history", medicineHistoryRepository.findByUserIdOrderByTimestampDesc(followedUserId));

//        // Gửi thông báo khi dữ liệu sức khỏe được truy cập
//        User followedUser = authRepository.findById(followedUserId)
//                .orElseThrow(() -> new RuntimeException("Followed user not found"));
//        String title = "Dữ liệu sức khỏe được xem!";
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//        String formattedTime = sdf.format(new java.util.Date()); // 05:24 PM +07, 22/05/2025
//        String body = (followerUser.getName() != null ? followerUser.getName() : "Người dùng") +
//                " vừa xem dữ liệu sức khỏe của bạn vào " + formattedTime + ".";
//        try {
//            notificationService.sendNotificationToUser(followedUserId, title, body);
//            logger.info("Notification sent to followedUserId: {} with title: {}", followedUserId, title);
//        } catch (Exception e) {
//            logger.error("Failed to send notification to followedUserId: {}. Error: {}", followedUserId, e.getMessage(), e);
//        }

        return ResponseEntity.ok(healthData);
    }
}