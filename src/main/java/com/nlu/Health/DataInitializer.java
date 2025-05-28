package com.nlu.Health;

import com.nlu.Health.model.MedicineReminder;
import com.nlu.Health.model.Prescription;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.MedicineReminderRepository;
import com.nlu.Health.repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private MedicineReminderRepository medicineReminderRepository;

    @Autowired
    private AuthRepository authRepository;

    @Bean
    public ApplicationRunner initMedicineReminders() {
        return args -> {
            logger.info("Starting DataInitializer to create MedicineReminders for today...");
            createRemindersForToday();
            logger.info("Initialized medicine reminders for today.");
        };
    }

    private void createRemindersForToday() {
        List<Prescription> prescriptions = prescriptionRepository.findAll();
        if (prescriptions.isEmpty()) {
            logger.warn("No prescriptions found to initialize medicine reminders.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy");
        dateOnlySdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // Lấy ngày hiện tại (22/05/2025)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Date today = calendar.getTime();
        String todayStr = dateOnlySdf.format(today); // "22/05/2025"
        logger.info("Today is: {}", todayStr);

        for (Prescription prescription : prescriptions) {
            String userId = prescription.getUserId();
            scheduleRemindersForToday(userId, prescription, sdf, dateOnlySdf, todayStr, today);
        }
    }

    private void scheduleRemindersForToday(String userId, Prescription prescription, SimpleDateFormat sdf, SimpleDateFormat dateOnlySdf, String todayStr, Date today) {
        Prescription.RepeatDetails repeat = prescription.getRepeatDetails();
        List<String> times = repeat.getTimePerDay();
        try {
            Date startDate = dateOnlySdf.parse(prescription.getStartday() + " 00:00");
            boolean isScheduledToday = false;
            if ("daily".equals(repeat.getType())) {
                isScheduledToday = !today.before(startDate);
            } else if ("weekly".equals(repeat.getType())) {
                List<String> daysOfWeek = repeat.getDaysOfWeek();
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
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
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
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
                    // Ghép ngày hôm nay với giờ từ timePerDay
                    String scheduledTimeStr = todayStr + " " + time;
                    Date scheduledTime = sdf.parse(scheduledTimeStr);
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

    private String getFcmToken(String userId) {
        return authRepository.findById(userId).get().getFcmToken();
    }
}