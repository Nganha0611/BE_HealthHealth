//package com.nlu.Health.service;
//
//import com.nlu.Health.model.MedicalHistory;
//import com.nlu.Health.model.Notification;
//import com.nlu.Health.model.TrackingPermission;
//import com.nlu.Health.repository.AuthRepository;
//import com.nlu.Health.repository.MedicalHistoryRepository;
//import com.nlu.Health.repository.NotificationRepository;
//import com.nlu.Health.repository.TrackingPermissionRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.TimeZone;
//
//@RestController
//@Service
//public class MedicalHistoryReminderService {
//
//    private static final Logger logger = LoggerFactory.getLogger(MedicalHistoryReminderService.class);
//
//    @Autowired
//    private MedicalHistoryRepository medicalHistoryRepository;
//
//    @Autowired
//    private TrackingPermissionRepository trackingPermissionRepository;
//
//    @Autowired
//    private NotificationService notificationService;
//
//    @Autowired
//    private AuthRepository authRepository;
//    @Autowired
//    private NotificationRepository notificationRepository;
//
//
//    @Scheduled(cron = "0 0 0 * * *") // Chạy mỗi ngày lúc 00:00
//    public void checkMedicalHistoryReminders() {
//        logger.info("Checking medical history reminders at: {}", new Date());
//
//        // Lấy ngày hiện tại ở múi giờ Asia/Ho_Chi_Minh
//        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//        Date today = calendar.getTime();
//
//        // Đặt calendar về 00:00:00 để so sánh ngày
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        Date todayStart = calendar.getTime();
//
//        // Lấy tất cả MedicalHistory có trạng thái PENDING
//        List<MedicalHistory> histories = medicalHistoryRepository.findByStatus("PENDING");
//        if (histories.isEmpty()) {
//            logger.info("No PENDING medical histories found for reminder check.");
//            return;
//        }
//
//        logger.info("Found {} PENDING medical histories to check for reminders", histories.size());
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//
//        for (MedicalHistory history : histories) {
//            Date appointmentDate = history.getAppointmentDate();
//            if (appointmentDate == null) {
//                logger.warn("Appointment date is null for medical history ID: {}", history.getId());
//                continue;
//            }
//
//            // Chuyển appointmentDate về 00:00:00 để so sánh ngày
//            Calendar appointmentCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//            appointmentCal.setTime(appointmentDate);
//            appointmentCal.set(Calendar.HOUR_OF_DAY, 0);
//            appointmentCal.set(Calendar.MINUTE, 0);
//            appointmentCal.set(Calendar.SECOND, 0);
//            appointmentCal.set(Calendar.MILLISECOND, 0);
//            Date appointmentDayStart = appointmentCal.getTime();
//
//            // Tính ngày trước và sau 1 ngày
//            appointmentCal.add(Calendar.DAY_OF_MONTH, -1);
//            Date oneDayBefore = appointmentCal.getTime();
//            appointmentCal.add(Calendar.DAY_OF_MONTH, 2); // +2 để đến ngày sau
//            Date oneDayAfter = appointmentCal.getTime();
//
//            String userId = history.getUserId();
//            String location = history.getLocation();
//            String appointmentDateStr = sdf.format(appointmentDate);
//
//            // Kiểm tra nếu là ngày trước 1 ngày
//            if (todayStart.equals(oneDayBefore)) {
//                String title = "Nhắc nhở lịch khám bệnh!";
//                String body = "Bạn có lịch khám bệnh vào ngày " + appointmentDateStr + " tại " + location + ".";
//                String bodyFollower = authRepository.findById(userId).get().getName() + " có lịch khám bệnh vào ngày " + appointmentDateStr + " tại " + location + ".";
//                sendReminderToUserAndFollowers(userId, title, body, bodyFollower);
//                Notification notification = new Notification(
//                        userId,
//                        "appointment",
//                        body,
//                        LocalDateTime.now(),
//                        "unread"
//                );
//                notificationRepository.save(notification);
//                logger.info("Sent pre-appointment reminder for medical history ID: {}", history.getId());
//            }
//
//            // Kiểm tra nếu là ngày sau 1 ngày và trạng thái là PENDING
//            if (todayStart.equals(oneDayAfter) && "PENDING".equals(history.getStatus())) {
//                history.setStatus("MISSED");
//                medicalHistoryRepository.save(history);
//                logger.info("Updated medical history ID: {} to MISSED status", history.getId());
//
//                String title = "Cảnh báo bỏ lỡ lịch khám bệnh!";
//                String body = authRepository.findById(userId).get().getName() + " đã bỏ lỡ lịch khám bệnh vào ngày " +
//                        appointmentDateStr + " tại " + location + ".";
//                String bodyFollower = authRepository.findById(userId).get().getName() + " đã bỏ lỡ lịch khám bệnh vào ngày " +
//                        appointmentDateStr + " tại " + location + ".";
//                sendReminderToUserAndFollowers(userId, title, body, bodyFollower);
//                logger.info("Sent missed appointment reminder for medical history ID: {}", history.getId());
//            }
//        }
//    }
//
//    private void sendReminderToUserAndFollowers(String userId, String title, String bodyUser, String bodyFollower) {
//        // Gửi thông báo cho người dùng
//        try {
//            notificationService.sendNotificationToUser(userId, title, bodyUser);
//            logger.info("Sent reminder to userId: {}", userId);
//        } catch (Exception e) {
//            logger.error("Failed to send reminder to userId: {}. Error: {}", userId, e.getMessage(), e);
//        }
//
//        // Gửi thông báo cho người theo dõi
//        List<TrackingPermission> followers = trackingPermissionRepository.findByFollowedUserIdAndStatus(userId, "approved");
//        for (TrackingPermission permission : followers) {
//            String followerId = permission.getFollowerUserId();
//            try {
//                notificationService.sendNotificationToUser(followerId, title, bodyFollower);
//                logger.info("Sent reminder to followerId: {} for userId: {}", followerId, userId);
//            } catch (Exception e) {
//                logger.error("Failed to send reminder to followerId: {}. Error: {}", followerId, e.getMessage(), e);
//            }
//        }
//    }
//
//    @PostMapping("/api/test-medical-reminders")
//    public String testCheckMedicalHistoryReminders() {
//        checkMedicalHistoryReminders();
//        return "Checked medical history reminders manually at " + new Date();
//    }
//}