package com.nlu.Health.controller;

import com.nlu.Health.model.MedicineHistory;
import com.nlu.Health.model.MedicineReminder;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.MedicineHistoryRepository;
import com.nlu.Health.repository.MedicineReminderRepository;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicine-history")
public class MedicineHistoryController {

    @Autowired
    private MedicineHistoryRepository medicineHistoryRepository;

    @Autowired
    private MedicineReminderRepository medicineReminderRepository; // Thêm repository này

    @Autowired
    private AuthService authService;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        if (email == null) return null;

        User user = authService.getUsersByEmail(email);
        return user != null ? user.getId() : null;
    }

    @GetMapping
    public ResponseEntity<List<MedicineHistory>> getAllByUser(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(medicineHistoryRepository.findByUserIdOrderByTimestampDesc(userId));
    }

    @PostMapping
    public ResponseEntity<MedicineHistory> create(@RequestBody MedicineHistory history, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        history.setUserId(userId);
        MedicineHistory savedHistory = medicineHistoryRepository.save(history);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineHistory> update(@PathVariable String id, @RequestBody MedicineHistory newHistory, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MedicineHistory history = medicineHistoryRepository.findByIdAndUserId(id, userId);
        if (history == null) return ResponseEntity.notFound().build();

        String oldStatus = history.getStatus(); // Lưu trạng thái cũ
        history.setMedicineName(newHistory.getMedicineName()); // Cập nhật medicineName
        history.setStatus(newHistory.getStatus());
        history.setNote(newHistory.getNote());
        history.setTimestamp(newHistory.getTimestamp());
        MedicineHistory savedHistory = medicineHistoryRepository.save(history);

        // Kiểm tra và cập nhật MedicineReminder nếu status thay đổi thành "COMPLETED"
        if ("COMPLETED".equals(savedHistory.getStatus()) && !("COMPLETED".equals(oldStatus))) {
            String medicineHistoryId = savedHistory.getId();
            MedicineReminder reminder = medicineReminderRepository.findByMedicineHistoryId(medicineHistoryId);
            if (reminder != null && "PENDING".equals(reminder.getStatus())) {
                reminder.setStatus("COMPLETED");
                medicineReminderRepository.save(reminder);
                System.out.println("Updated MedicineReminder to COMPLETED for medicineHistoryId: " + medicineHistoryId);
            }
        }

        return ResponseEntity.ok(savedHistory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MedicineHistory history = medicineHistoryRepository.findByIdAndUserId(id, userId);
        if (history == null) return ResponseEntity.notFound().build();

        medicineHistoryRepository.delete(history);
        return ResponseEntity.noContent().build();
    }
}