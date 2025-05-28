package com.nlu.Health.controller;

import com.nlu.Health.model.MedicineReminder;
import com.nlu.Health.model.Prescription;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.MedicineReminderRepository;
import com.nlu.Health.repository.PrescriptionRepository;
import com.nlu.Health.service.NotificationService;
import com.nlu.Health.tools.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicine-reminders")
public class MedicineReminderController {

    @Autowired
    private MedicineReminderRepository medicineReminderRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AuthRepository authRepository;

    @PostMapping("/schedule")
    public ResponseEntity<Map<String, String>> scheduleReminder(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) {
        String token = authHeader.substring(7);
        String userEmail = JwtUtil.validateToken(token);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "unauthorized", "message", "Token không hợp lệ"));
        }

        String userId = getUserIdFromEmail(userEmail);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "userNotFound", "message", "Không tìm thấy tài khoản"));
        }

        String prescriptionId = payload.get("prescriptionId");
        Prescription prescription = prescriptionRepository.findById(prescriptionId).orElse(null);
        if (prescription == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "notFound", "message", "Không tìm thấy đơn thuốc"));
        }

        scheduleRemindersFromPrescription(userId, prescription);
        return ResponseEntity.ok(Map.of("result", "success", "message", "Đã lên lịch nhắc nhở"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateReminder(
            @PathVariable String id,
            @RequestBody Map<String, String> status) {
        MedicineReminder reminder = medicineReminderRepository.findById(id).orElse(null);
        if (reminder == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", "notFound", "message", "Không tìm thấy nhắc nhở"));
        }

        String newStatus = status.get("status");
        if ("COMPLETED".equals(newStatus)) {
            reminder.setStatus("COMPLETED");
        } else if ("MISSED".equals(newStatus)) {
            reminder.setStatus("MISSED");
        }
        medicineReminderRepository.save(reminder);
        return ResponseEntity.ok(Map.of("result", "success", "message", "Cập nhật trạng thái thành công"));
    }

    private String getUserIdFromEmail(String email) {
        return authRepository.findByEmail(email).getId(); // Thêm @Autowired private AuthRepository authRepository nếu cần
    }

    private void scheduleRemindersFromPrescription(String userId, Prescription prescription) {
        Prescription.RepeatDetails repeat = prescription.getRepeatDetails();
        List<String> times = repeat.getTimePerDay();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();
        try {
            Date startDate = sdf.parse(prescription.getStartday() + " 00:00");
            calendar.setTime(startDate);

            if ("weekly".equals(repeat.getType())) {
                List<String> daysOfWeek = repeat.getDaysOfWeek();
                for (String time : times) {
                    for (String day : daysOfWeek) {
                        int dayOfWeekIndex = getDayOfWeekIndex(day);
                        calendar.setTime(startDate);
                        while (!calendar.after(new Date())) {
                            if (calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeekIndex) {
                                Date scheduledTime = sdf.parse(sdf.format(calendar.getTime()) + " " + time);
                                createReminder(userId, prescription, scheduledTime);
                            }
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);
                        }
                    }
                }
            } else if ("monthly".equals(repeat.getType())) {
                List<String> daysOfMonth = repeat.getDaysOfMonth();
                for (String time : times) {
                    for (String day : daysOfMonth) {
                        calendar.setTime(startDate);
                        while (!calendar.after(new Date())) {
                            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                            Date scheduledTime = sdf.parse(sdf.format(calendar.getTime()) + " " + time);
                            createReminder(userId, prescription, scheduledTime);
                            calendar.add(Calendar.MONTH, 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createReminder(String userId, Prescription prescription, Date scheduledTime) {
        MedicineReminder reminder = new MedicineReminder();
        reminder.setUserId(userId);
        reminder.setPrescriptionId(prescription.getId());
        reminder.setMedicineName(prescription.getName());
        reminder.setScheduledTime(scheduledTime);
        reminder.setStatus("PENDING");
        reminder.setReminderCount(0);
        reminder.setFcmToken(getFcmToken(userId));
        medicineReminderRepository.save(reminder);
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
        return authRepository.findById(userId).get().getFcmToken(); // Thêm @Autowired private AuthRepository authRepository nếu cần
    }
}