package com.nlu.Health.service;

import com.nlu.Health.model.MedicineHistory;
import com.nlu.Health.model.MedicineReminder;
import com.nlu.Health.model.Notification;
import com.nlu.Health.model.TrackingPermission;
import com.nlu.Health.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RestController
@Service
public class MedicineReminderService {

    private static final Logger logger = LoggerFactory.getLogger(MedicineReminderService.class);

    @Autowired
    private MedicineReminderRepository medicineReminderRepository;

    @Autowired
    private MedicineHistoryRepository medicineHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TrackingPermissionRepository trackingPermissionRepository;

//    private static final int[] REMINDER_HOURS = {6, 8, 10, 12, 14, 17, 19, 20, 22, 23};
    private static final int MAX_REMINDERS = 3; // Giới hạn tối đa 3 lần nhắc
    @Autowired
    private AuthRepository authRepository;

    @Scheduled(cron = "0 0/5 * * * ?") // Chạy mỗi 5 phút
    public void checkMedicineReminders() {
        logger.info("Checking medicine reminders at: {}", new Date());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Date now = calendar.getTime();

        // Đặt khoảng thời gian kiểm tra: 5 phút trước đến 5 phút sau thời gian hiện tại
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date startTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10); // +10 phút từ startTime
        Date endTime = calendar.getTime();

        logger.info("Checking time range: {} to {}", startTime, endTime);

        List<MedicineReminder> reminders = medicineReminderRepository.findByStatusAndScheduledTimeBetween("PENDING", startTime, endTime);
        if (reminders.isEmpty()) {
            logger.info("No pending medicine reminders found for the current time slot.");
            return;
        }

        logger.info("Found {} medicine reminders to notify", reminders.size());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        for (MedicineReminder reminder : reminders) {
            String userId = reminder.getUserId();
            String medicineName = reminder.getMedicineName();
            Date scheduledTime = reminder.getScheduledTime();

            // Kiểm tra xem MedicineHistory đã tồn tại chưa
            MedicineHistory history;
            if (reminder.getMedicineHistoryId() == null) {
                // Lần đầu tiên: Tạo mới MedicineHistory
                history = new MedicineHistory();
                history.setUserId(userId);
                history.setMedicineName(medicineName);
                history.setTimestamp(scheduledTime);
                history.setStatus("PENDING");
                history.setNote("Created automatically by system");
                medicineHistoryRepository.save(history);

                // Lưu ID của MedicineHistory vào MedicineReminder
                reminder.setMedicineHistoryId(history.getId());
            } else {
                // Các lần sau: Cập nhật MedicineHistory hiện có
                history = medicineHistoryRepository.findById(reminder.getMedicineHistoryId()).orElse(null);
                if (history == null) {
                    logger.error("MedicineHistory not found for ID: {}", reminder.getMedicineHistoryId());
                    continue;
                }
            }

            // Gửi thông báo mỗi 5 phút
            reminder.setReminderCount(reminder.getReminderCount() + 1);
            medicineReminderRepository.save(reminder);

            String title = "Nhắc nhở uống thuốc!";
            String body;
            if (reminder.getReminderCount() == 1) {
                body = "Đã tới giờ uống " + medicineName + " vào lúc " + sdf.format(scheduledTime) + ".";
            } else {
                body = "Bạn đã quên uống " + medicineName + " vào lúc " + sdf.format(scheduledTime) +
                        ". (Lần nhắc nhở " + reminder.getReminderCount() + "/" + MAX_REMINDERS + ")";
            }
            try {
                Notification notification = new Notification(
                        userId,
                        "medication_reminder",
                        body,
                        LocalDateTime.now(),
                        "unread"
                );
                notificationService.sendNotificationToUser(userId, title, body, notification);

                logger.info("Sent reminder to userId: {} for medicine: {}", userId, medicineName);
            } catch (Exception e) {
                logger.error("Failed to send reminder to userId: {}. Error: {}", userId, e.getMessage(), e);
            }

            if (reminder.getReminderCount() >= MAX_REMINDERS && "PENDING".equals(reminder.getStatus())) {
                reminder.setStatus("MISSED");
                medicineReminderRepository.save(reminder);

                if (history != null) {
                    history.setStatus("Missing");
                    medicineHistoryRepository.save(history);
                }

                sendMissedNotificationToFollowers(userId, medicineName, scheduledTime);
            }
        }
    }

    private void sendMissedNotificationToFollowers(String userId, String medicineName, Date scheduledTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String title = "Cảnh báo quên uống thuốc!";
        String body = authRepository.findById(userId).get().getName() + " đã quên uống " + medicineName + " vào lúc " + sdf.format(scheduledTime);
        List<TrackingPermission> followers = trackingPermissionRepository.findByFollowedUserIdAndStatus(userId, "approved");
        for (TrackingPermission permission : followers) {
            String followerId = permission.getFollowerUserId();
            try {

                Notification notification = new Notification(
                        followerId,
                        "medication_reminder",
                        body,
                        LocalDateTime.now(),
                        "unread"
                );
                notificationService.sendNotificationToUser(followerId, title, body, notification);

                logger.info("Sent missed notification to followerId: {} for userId: {}", followerId, userId);
            } catch (Exception e) {
                logger.error("Failed to send missed notification to followerId: {}. Error: {}", followerId, e.getMessage(), e);
            }
        }
    }

    @PostMapping("/api/test-reminders")
    public String testCheckMedicineReminders() {
        checkMedicineReminders();
        return "Checked medicine reminders manually at " + new Date();
    }
}