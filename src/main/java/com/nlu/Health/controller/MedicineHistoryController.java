package com.nlu.Health.controller;

import com.nlu.Health.model.MedicineHistory;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.MedicineHistoryRepository;
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
    private AuthRepository authRepository;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        if (email == null) return null;

        User user = authRepository.findByEmail(email);
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
        System.out.println("Timestamp từ request (POST): " + history.getTimestamp());
        MedicineHistory savedHistory = medicineHistoryRepository.save(history);
        System.out.println("Timestamp sau khi lưu (POST): " + savedHistory.getTimestamp());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineHistory> update(@PathVariable String id, @RequestBody MedicineHistory newHistory, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MedicineHistory history = medicineHistoryRepository.findByIdAndUserId(id, userId);
        if (history == null) return ResponseEntity.notFound().build();

        history.setMedicineName(newHistory.getMedicineName()); // Cập nhật medicineName
        history.setStatus(newHistory.getStatus());
        history.setNote(newHistory.getNote());
        history.setTimestamp(newHistory.getTimestamp());
        System.out.println("Timestamp từ request (PUT): " + newHistory.getTimestamp());
        MedicineHistory savedHistory = medicineHistoryRepository.save(history);
        System.out.println("Timestamp sau khi lưu (PUT): " + savedHistory.getTimestamp());
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