package com.nlu.Health.service;

import com.nlu.Health.model.MedicalHistory;
import com.nlu.Health.model.MedicineReminder;
import com.nlu.Health.model.Notification;
import com.nlu.Health.model.Prescription;
import com.nlu.Health.model.TrackingPermission;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.MedicalHistoryRepository;
import com.nlu.Health.repository.MedicineReminderRepository;
import com.nlu.Health.repository.NotificationRepository;
import com.nlu.Health.repository.PrescriptionRepository;
import com.nlu.Health.repository.TrackingPermissionRepository;
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
public class HealthReminderSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(HealthReminderSchedulerService.class);

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private MedicineReminderRepository medicineReminderRepository;

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private TrackingPermissionRepository trackingPermissionRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // Định dạng ngày và múi giờ chung
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy");
    private final TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    public HealthReminderSchedulerService() {
        sdfDateTime.setTimeZone(timeZone);
        sdfDateOnly.setTimeZone(timeZone);
    }

    @Scheduled(cron = "0 0 0 * * *") // Chạy mỗi ngày vào 00:00
    public void scheduleDailyReminders() {
        logger.info("Starting daily health reminders scheduling at: {}", new Date());

        // Tạo nhắc nhở uống thuốc
        scheduleMedicineReminders();
        logger.info("Completed scheduling daily medicine reminders.");

        // Kiểm tra và gửi nhắc nhở lịch khám bệnh
        checkMedicalHistoryReminders();
        logger.info("Completed checking medical history reminders.");
    }

    // Logic tạo nhắc nhở uống thuốc (từ ReminderSchedulerService)
    private void scheduleMedicineReminders() {
        List<Prescription> prescriptions = prescriptionRepository.findAll();
        if (prescriptions.isEmpty()) {
            logger.warn("No prescriptions found to schedule reminders.");
            return;
        }

        Calendar calendar = Calendar.getInstance(timeZone);
        Date today = calendar.getTime();
        String todayStr = sdfDateOnly.format(today); // "03/06/2025"
        logger.info("Today is: {}", todayStr);

        for (Prescription prescription : prescriptions) {
            String userId = prescription.getUserId();
            scheduleRemindersForToday(userId, prescription, todayStr, today);
        }
    }

    private void scheduleRemindersForToday(String userId, Prescription prescription, String todayStr, Date today) {
        Prescription.RepeatDetails repeat = prescription.getRepeatDetails();
        List<String> times = repeat.getTimePerDay();
        try {
            Date startDate = sdfDateOnly.parse(prescription.getStartday() + " 00:00");
            boolean isScheduledToday = false;
            if ("daily".equals(repeat.getType())) {
                isScheduledToday = !today.before(startDate);
            } else if ("weekly".equals(repeat.getType())) {
                List<String> daysOfWeek = repeat.getDaysOfWeek();
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(today);
                int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                for (String day : daysOfWeek) {
                    if (todayDayOfWeek == getDayOfWeekIndex(day) && !today.before(startDate)) {
                        isScheduledToday = true;
                        break;
                    }
                }
            } else if ("monthly".equals(repeat.getType())) {
                List<String> daysOfMonth = repeat.getDaysOfMonth();
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(today);
                int todayDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                for (String day : daysOfMonth) {
                    if (todayDayOfMonth == Integer.parseInt(day) && !today.before(startDate)) {
                        isScheduledToday = true;
                        break;
                    }
                }
            } else {
                logger.warn("Unsupported repeat type: {} for prescription: {}", repeat.getType(), prescription.getId());
                return;
            }

            if (isScheduledToday) {
                for (String time : times) {
                    String scheduledTimeStr = todayStr + " " + time;
                    Date scheduledTime = sdfDateTime.parse(scheduledTimeStr);
                    createReminderIfNotExists(userId, prescription, scheduledTime);
                }
            } else {
                logger.info("No reminders scheduled for today for prescription: {}", prescription.getId());
            }
        } catch (Exception e) {
            logger.error("Error scheduling reminders for prescription {}: {}", prescription.getId(), e.getMessage(), e);
        }
    }

    private void createReminderIfNotExists(String userId, Prescription prescription, Date scheduledTime) {
        List<MedicineReminder> existingReminders = medicineReminderRepository.findByUserIdAndPrescriptionIdAndScheduledTime(userId, prescription.getId(), scheduledTime);
        if (existingReminders.isEmpty()) {
            MedicineReminder reminder = new MedicineReminder();
            reminder.setUserId(userId);
            reminder.setPrescriptionId(prescription.getId());
            reminder.setMedicineName(prescription.getName());
            reminder.setScheduledTime(scheduledTime);
            reminder.setStatus("PENDING");
            reminder.setReminderCount(0);
            reminder.setFcmToken(getFcmToken(userId));
            reminder.setMedicineHistoryId(null);
            medicineReminderRepository.save(reminder);
            logger.info("Created new MedicineReminder: userId={}, prescriptionId={}, scheduledTime={}",
                    userId, prescription.getId(), scheduledTime);
        } else {
            logger.info("Reminder already exists for userId={}, prescriptionId={}, scheduledTime={}",
                    userId, prescription.getId(), scheduledTime);
        }
    }

    private int getDayOfWeekIndex(String day) {
        switch (day.toUpperCase()) {
            case "MON": return Calendar.MONDAY;
            case "TUE": return Calendar.TUESDAY;
            case "WED": return Calendar.WEDNESDAY;
            case "THU": return Calendar.THURSDAY;
            case "FRI": return Calendar.FRIDAY;
            case "SAT": return Calendar.SATURDAY;
            case "SUN": return Calendar.SUNDAY;
            default: return Calendar.MONDAY;
        }
    }

    // Logic kiểm tra lịch khám bệnh (từ MedicalHistoryReminderService)
    private void checkMedicalHistoryReminders() {
        // Lấy ngày hiện tại ở múi giờ Asia/Ho_Chi_Minh
        Calendar calendar = Calendar.getInstance(timeZone);
        Date today = calendar.getTime();

        // Đặt calendar về 00:00:00 để so sánh ngày
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        // Lấy tất cả MedicalHistory có trạng thái PENDING
        List<MedicalHistory> histories = medicalHistoryRepository.findByStatus("PENDING");
        if (histories.isEmpty()) {
            logger.info("No PENDING medical histories found for reminder check.");
            return;
        }

        logger.info("Found {} PENDING medical histories to check for reminders", histories.size());

        for (MedicalHistory history : histories) {
            Date appointmentDate = history.getAppointmentDate();
            if (appointmentDate == null) {
                logger.warn("Appointment date is null for medical history ID: {}", history.getId());
                continue;
            }

            // Chuyển appointmentDate về 00:00:00 để so sánh ngày
            Calendar appointmentCal = Calendar.getInstance(timeZone);
            appointmentCal.setTime(appointmentDate);
            appointmentCal.set(Calendar.HOUR_OF_DAY, 0);
            appointmentCal.set(Calendar.MINUTE, 0);
            appointmentCal.set(Calendar.SECOND, 0);
            appointmentCal.set(Calendar.MILLISECOND, 0);
            Date appointmentDayStart = appointmentCal.getTime();

            // Tính ngày trước và sau 1 ngày
            appointmentCal.add(Calendar.DAY_OF_MONTH, -1);
            Date oneDayBefore = appointmentCal.getTime();
            appointmentCal.add(Calendar.DAY_OF_MONTH, 2); // +2 để đến ngày sau
            Date oneDayAfter = appointmentCal.getTime();

            String userId = history.getUserId();
            String location = history.getLocation();
            String appointmentDateStr = sdfDateOnly.format(appointmentDate);

            // Kiểm tra nếu là ngày trước 1 ngày
            if (todayStart.equals(oneDayBefore)) {
                String title = "Nhắc nhở lịch khám bệnh!";
                String bodyForUser = "Bạn có lịch khám bệnh vào ngày " + appointmentDateStr + " tại " + location + ".";
                String bodyFollower = authRepository.findById(userId).get().getName() + " có lịch khám bệnh vào ngày " + appointmentDateStr + " tại " + location + ".";
                Notification notificationForUser = new Notification(
                        userId,
                        "appointment",
                        bodyForUser,
                        LocalDateTime.now(),
                        "unread"
                );
                Notification notificationForFollowers = new Notification(
                        userId,
                        "appointment",
                        bodyFollower,
                        LocalDateTime.now(),
                        "unread"
                );
                sendReminderToUserAndFollowers(userId, title, bodyForUser, bodyFollower, notificationForUser, notificationForFollowers);

                logger.info("Sent pre-appointment reminder for medical history ID: {}", history.getId());
            }

            // Kiểm tra nếu là ngày sau 1 ngày và trạng thái là PENDING
            if (todayStart.equals(oneDayAfter) && "PENDING".equals(history.getStatus())) {
                history.setStatus("MISSED");
                medicalHistoryRepository.save(history);
                logger.info("Updated medical history ID: {} to MISSED status", history.getId());

                String title = "Cảnh báo bỏ lỡ lịch khám bệnh!";
                String bodyForUser = "Bạn đã bỏ lỡ lịch khám bệnh vào ngày " +
                        appointmentDateStr + " tại " + location + ".";
                String bodyFollower = authRepository.findById(userId).get().getName() + " đã bỏ lỡ lịch khám bệnh vào ngày " +
                        appointmentDateStr + " tại " + location + ".";
                Notification notificationForUser = new Notification(
                        userId,
                        "appointment",
                        bodyForUser,
                        LocalDateTime.now(),
                        "unread"
                );
                Notification notificationForFollowers = new Notification(
                        userId,
                        "appointment",
                        bodyFollower,
                        LocalDateTime.now(),
                        "unread"
                );
                sendReminderToUserAndFollowers(userId, title, bodyForUser, bodyFollower, notificationForUser, notificationForFollowers);
                logger.info("Sent missed appointment reminder for medical history ID: {}", history.getId());
            }
        }
    }

    private void sendReminderToUserAndFollowers(String userId, String title, String bodyUser, String bodyFollower, Notification notificationForUser, Notification notificationForFollowers) {
        try {
            notificationService.sendNotificationToUser(userId, title, bodyUser, notificationForUser);
            notificationService.sendNotificationToFollowers(userId, title, bodyFollower, notificationForFollowers);
            logger.info("Sent reminder to userId: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to send reminder to userId: {}. Error: {}", userId, e.getMessage(), e);
        }
    }

    private String getFcmToken(String userId) {
        return authRepository.findById(userId).get().getFcmToken();
    }

    @PostMapping("/api/test-health-reminders")
    public String testHealthReminders() {
        scheduleDailyReminders();
        return "Checked health reminders manually at " + new Date();
    }
}
//package com.nlu.Health.service;
//
//import com.nlu.Health.model.MedicineReminder;
//import com.nlu.Health.model.Prescription;
//import com.nlu.Health.repository.AuthRepository;
//import com.nlu.Health.repository.MedicineReminderRepository;
//import com.nlu.Health.repository.PrescriptionRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.TimeZone;
//
//@Service
//public class ReminderSchedulerService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ReminderSchedulerService.class);
//
//    @Autowired
//    private PrescriptionRepository prescriptionRepository;
//
//    @Autowired
//    private MedicineReminderRepository medicineReminderRepository;
//
//    @Autowired
//    private AuthRepository authRepository;
//
//    @Scheduled(cron = "0 0 0 * * *") // Chạy mỗi ngày vào 00:00
//    public void scheduleDailyReminders() {
//        logger.info("Scheduling daily medicine reminders at: {}", new Date());
//        createRemindersForToday();
//        logger.info("Completed scheduling daily medicine reminders.");
//    }
//
//    private void createRemindersForToday() {
//        List<Prescription> prescriptions = prescriptionRepository.findAll();
//        if (prescriptions.isEmpty()) {
//            logger.warn("No prescriptions found to schedule reminders.");
//            return;
//        }
//
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy");
//        dateOnlySdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//
//        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//        Date today = calendar.getTime();
//        String todayStr = dateOnlySdf.format(today); // "22/05/2025"
//        logger.info("Today is: {}", todayStr);
//
//        for (Prescription prescription : prescriptions) {
//            String userId = prescription.getUserId();
//            scheduleRemindersForToday(userId, prescription, sdf, dateOnlySdf, todayStr, today);
//        }
//    }
//
//    private void scheduleRemindersForToday(String userId, Prescription prescription, SimpleDateFormat sdf, SimpleDateFormat dateOnlySdf, String todayStr, Date today) {
//        Prescription.RepeatDetails repeat = prescription.getRepeatDetails();
//        List<String> times = repeat.getTimePerDay();
//        try {
//            Date startDate = dateOnlySdf.parse(prescription.getStartday() + " 00:00");
//            boolean isScheduledToday = false;
//            if ("daily".equals(repeat.getType())) {
//                isScheduledToday = !today.before(startDate);
//            } else if ("weekly".equals(repeat.getType())) {
//                List<String> daysOfWeek = repeat.getDaysOfWeek();
//                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//                calendar.setTime(today);
//                int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//                for (String day : daysOfWeek) {
//                    if (todayDayOfWeek == getDayOfWeekIndex(day) && !today.before(startDate)) {
//                        isScheduledToday = true;
//                        break;
//                    }
//                }
//            } else if ("monthly".equals(repeat.getType())) {
//                List<String> daysOfMonth = repeat.getDaysOfMonth();
//                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//                calendar.setTime(today);
//                int todayDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
//                for (String day : daysOfMonth) {
//                    if (todayDayOfMonth == Integer.parseInt(day) && !today.before(startDate)) {
//                        isScheduledToday = true;
//                        break;
//                    }
//                }
//            } else {
//                logger.warn("Unsupported repeat type: {} for prescription: {}", repeat.getType(), prescription.getId());
//                return;
//            }
//
//            if (isScheduledToday) {
//                for (String time : times) {
//                    String scheduledTimeStr = todayStr + " " + time;
//                    Date scheduledTime = sdf.parse(scheduledTimeStr);
//                    createReminderIfNotExists(userId, prescription, scheduledTime);
//                }
//            } else {
//                logger.info("No reminders scheduled for today for prescription: {}", prescription.getId());
//            }
//        } catch (Exception e) {
//            logger.error("Error scheduling reminders for prescription {}: {}", prescription.getId(), e.getMessage(), e);
//        }
//    }
//
//    private void createReminderIfNotExists(String userId, Prescription prescription, Date scheduledTime) {
//        List<MedicineReminder> existingReminders = medicineReminderRepository.findByUserIdAndPrescriptionIdAndScheduledTime(userId, prescription.getId(), scheduledTime);
//        if (existingReminders.isEmpty()) {
//            MedicineReminder reminder = new MedicineReminder();
//            reminder.setUserId(userId);
//            reminder.setPrescriptionId(prescription.getId());
//            reminder.setMedicineName(prescription.getName());
//            reminder.setScheduledTime(scheduledTime);
//            reminder.setStatus("PENDING");
//            reminder.setReminderCount(0);
//            reminder.setFcmToken(getFcmToken(userId));
//            reminder.setMedicineHistoryId(null);
//            medicineReminderRepository.save(reminder);
//            logger.info("Created new MedicineReminder: userId={}, prescriptionId={}, scheduledTime={}",
//                    userId, prescription.getId(), scheduledTime);
//        } else {
//            logger.info("Reminder already exists for userId={}, prescriptionId={}, scheduledTime={}",
//                    userId, prescription.getId(), scheduledTime);
//        }
//    }
//
//    private int getDayOfWeekIndex(String day) {
//        switch (day.toUpperCase()) {
//            case "MON": return Calendar.MONDAY;
//            case "TUE": return Calendar.TUESDAY;
//            case "WED": return Calendar.WEDNESDAY;
//            case "THU": return Calendar.THURSDAY;
//            case "FRI": return Calendar.FRIDAY;
//            case "SAT": return Calendar.SATURDAY;
//            case "SUN": return Calendar.SUNDAY;
//            default: return Calendar.MONDAY;
//        }
//    }
//
//    private String getFcmToken(String userId) {
//        return authRepository.findById(userId).get().getFcmToken();
//    }
//}