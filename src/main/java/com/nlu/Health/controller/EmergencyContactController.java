package com.nlu.Health.controller;

import com.nlu.Health.model.EmergencyContact;
import com.nlu.Health.repository.EmergencyContactRepository;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    @Autowired
    private AuthService authService;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String email = JwtUtil.validateToken(token);
        if (email == null) {
            return null;
        }
        User user = authService.getUsersByEmail(email);
        return user != null ? user.getId() : null;
    }

    // Lấy tất cả liên hệ khẩn cấp của người dùng
    @GetMapping
    public ResponseEntity<List<EmergencyContact>> getEmergencyContacts(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<EmergencyContact> contacts = emergencyContactRepository.findByUserId(userId);
        return ResponseEntity.ok(contacts);
    }

    // Thêm liên hệ khẩn cấp
    @PostMapping
    public ResponseEntity<EmergencyContact> addEmergencyContact(
            HttpServletRequest request,
            @RequestBody EmergencyContact contact) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Kiểm tra trùng lặp số điện thoại
        List<EmergencyContact> existingContacts = emergencyContactRepository.findByUserIdAndPhoneNumber(userId, contact.getPhoneNumber());
        if (!existingContacts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        contact.setUserId(userId);
        EmergencyContact savedContact = emergencyContactRepository.save(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmergencyContact(HttpServletRequest request, @PathVariable String id) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        emergencyContactRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}