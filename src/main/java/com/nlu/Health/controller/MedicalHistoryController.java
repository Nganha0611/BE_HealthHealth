package com.nlu.Health.controller;

import com.nlu.Health.model.MedicalHistory;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.MedicalHistoryRepository;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-history")
public class MedicalHistoryController {

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

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
    public ResponseEntity<List<MedicalHistory>> getAllByUser(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(medicalHistoryRepository.findByUserIdOrderByTimestampDesc(userId));
    }

    @PostMapping
    public ResponseEntity<MedicalHistory> create(@RequestBody MedicalHistory history, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        history.setUserId(userId);
        MedicalHistory savedHistory = medicalHistoryRepository.save(history);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalHistory> update(@PathVariable String id, @RequestBody MedicalHistory newHistory, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MedicalHistory history = medicalHistoryRepository.findByIdAndUserId(id, userId);
        if (history == null) return ResponseEntity.notFound().build();

        history.setAppointmentDate(newHistory.getAppointmentDate());
        history.setLocation(newHistory.getLocation());
        history.setNote(newHistory.getNote());
        history.setStatus(newHistory.getStatus());

        System.out.println("AppointmentDate từ request (PUT): " + newHistory.getAppointmentDate());
        MedicalHistory savedHistory = medicalHistoryRepository.save(history);
        System.out.println("AppointmentDate sau khi lưu (PUT): " + savedHistory.getAppointmentDate());
        return ResponseEntity.ok(savedHistory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MedicalHistory history = medicalHistoryRepository.findByIdAndUserId(id, userId);
        if (history == null) return ResponseEntity.notFound().build();

        medicalHistoryRepository.delete(history);
        return ResponseEntity.noContent().build();
    }
}
