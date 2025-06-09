package com.nlu.Health.controller;

import com.nlu.Health.model.Prescription;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.PrescriptionRepository;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AuthService authService;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("PrescriptionController - Authorization Header: " + token); // Logging để debug

        if (token == null) {
            System.out.println("PrescriptionController - Token is null");
            return null;
        }

        // Xử lý token linh hoạt hơn: bỏ qua prefix "Bearer " nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        System.out.println("PrescriptionController - Email from token: " + email);

        if (email == null) {
            System.out.println("PrescriptionController - Invalid token");
            return null;
        }

        User user = authService.getUsersByEmail(email);
        System.out.println("PrescriptionController - User ID: " + (user != null ? user.getId() : "null"));

        return user != null ? user.getId() : null;
    }

    @GetMapping
    public ResponseEntity<List<Prescription>> getAllPrescriptions(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Prescription> prescriptions = prescriptionRepository.findByUserIdOrderByNameAsc(userId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable String id, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription prescription = prescriptionRepository.findByIdAndUserId(id, userId);
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(prescription);
    }

    @PostMapping
    public ResponseEntity<Prescription> createPrescription(@RequestBody Prescription prescription, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        prescription.setUserId(userId);
        Prescription savedPrescription = prescriptionRepository.save(prescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPrescription);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prescription> updatePrescription(@PathVariable String id, @RequestBody Prescription prescriptionDetails, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription prescription = prescriptionRepository.findByIdAndUserId(id, userId);
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }

        prescription.setName(prescriptionDetails.getName());
        prescription.setForm(prescriptionDetails.getForm());
        prescription.setStrength(prescriptionDetails.getStrength());
        prescription.setUnit(prescriptionDetails.getUnit());
        prescription.setAmount(prescriptionDetails.getAmount());
        prescription.setInstruction(prescriptionDetails.getInstruction());
        prescription.setStartday(prescriptionDetails.getStartday());
        prescription.setRepeatDetails(prescriptionDetails.getRepeatDetails());

        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        return ResponseEntity.ok(updatedPrescription);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable String id, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription prescription = prescriptionRepository.findByIdAndUserId(id, userId);
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }

        prescriptionRepository.delete(prescription);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/search")
//    public ResponseEntity<List<Prescription>> searchPrescriptions(@RequestParam String name, HttpServletRequest request) {
//        String userId = getUserIdFromRequest(request);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        List<Prescription> prescriptions = prescriptionRepository.findByUserIdAndNameContainingIgnoreCase(userId, name);
//        return ResponseEntity.ok(prescriptions);
//    }
}