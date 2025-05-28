package com.nlu.Health.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "medicine_reminders")
public class MedicineReminder {
    @Id
    private String id;
    private String userId;
    private String prescriptionId;
    private String medicineName;
    private Date scheduledTime;
    private String status;           // PENDING, COMPLETED, MISSED
    private int reminderCount;       // Số lần đã nhắc nhở
    private String fcmToken;
    private String medicineHistoryId; // Thêm trường để lưu ID của MedicineHistory

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getReminderCount() { return reminderCount; }
    public void setReminderCount(int reminderCount) { this.reminderCount = reminderCount; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getMedicineHistoryId() { return medicineHistoryId; }
    public void setMedicineHistoryId(String medicineHistoryId) { this.medicineHistoryId = medicineHistoryId; }
}