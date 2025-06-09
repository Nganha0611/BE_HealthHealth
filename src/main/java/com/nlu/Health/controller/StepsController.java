package com.nlu.Health.controller;

import com.nlu.Health.model.Steps;
import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.repository.StepsRepository;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/steps")
public class StepsController {

    @Autowired
    private StepsRepository stepsRepo;

    @Autowired
    private AuthService authService;

    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("StepsController - Authorization Header: " + token);

        if (token == null) {
            System.out.println("StepsController - Token is null");
            return null;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = JwtUtil.validateToken(token);
        System.out.println("StepsController - Email from token: " + email);

        if (email == null) {
            System.out.println("StepsController - Invalid token");
            return null;
        }

        User user = authService.getUsersByEmail(email);
        System.out.println("StepsController - User ID: " + (user != null ? user.getId() : "null"));

        return user != null ? user.getId() : null;
    }

    @PostMapping("/measure")
    public ResponseEntity<Steps> createSteps(@RequestBody Steps steps, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            System.out.println("StepsController - Unauthorized: Invalid token or user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        steps.setUserId(userId);

        if (steps.getCreatedAt() == null) {
            ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
            steps.setCreatedAt(Date.from(currentTime.toInstant()));
        }

        Steps savedSteps = stepsRepo.save(steps);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSteps);
    }

    @GetMapping("/user/{userId}/daily-total")
    public ResponseEntity<Long> getDailyStepsTotal(@PathVariable String userId, HttpServletRequest request) {
        String authenticatedUserId = getUserIdFromRequest(request);
        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authenticatedUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        Date startDate = Date.from(startOfDay.toInstant());
        Date endDate = Date.from(now.toInstant());

        Optional<Steps> latestSteps = stepsRepo.findFirstByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);

        if (latestSteps.isEmpty()) {
            return ResponseEntity.ok(0L);
        }

        return ResponseEntity.ok((long) latestSteps.get().getSteps());
    }

    @GetMapping("/measure/latest")
    public ResponseEntity<Steps> getLatestSteps(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Steps latest = stepsRepo.findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (latest == null) {
            System.out.println("StepsController - No steps data found for userId: " + userId);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(latest);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Steps>> getStepsByUser(@PathVariable String userId, HttpServletRequest request) {
        String authenticatedUserId = getUserIdFromRequest(request);
        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Steps> steps = stepsRepo.findByUserIdOrderByCreatedAtAsc(userId);
        return ResponseEntity.ok(steps);
    }

    @DeleteMapping("/measure/delete-by-date")
    public ResponseEntity<Void> deleteStepsByDate(
            @RequestParam String date,
            HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId == null) {
            System.out.println("StepsController - Unauthorized: Invalid token or user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Parse ngày từ query param (định dạng ISO: "2025-05-22T00:00:00.000Z")
            ZonedDateTime dateTime = ZonedDateTime.parse(date).withZoneSameInstant(ZoneId.of("UTC"));
            ZonedDateTime startOfDay = dateTime.toLocalDate().atStartOfDay(ZoneId.of("UTC"));
            ZonedDateTime endOfDay = dateTime.toLocalDate().atTime(LocalTime.MAX).atZone(ZoneId.of("UTC"));

            Date startDate = Date.from(startOfDay.toInstant());
            Date endDate = Date.from(endOfDay.toInstant());

            System.out.println("StepsController - Deleting steps for userId: " + userId +
                    ", from: " + startDate + ", to: " + endDate);

            // Xóa tất cả bản ghi trong khoảng thời gian từ startOfDay đến endOfDay
            int deletedCount = stepsRepo.deleteByUserIdAndCreatedAtBetween(userId, startDate, endDate);
            System.out.println("StepsController - Deleted " + deletedCount + " steps records for userId: " + userId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("StepsController - Error deleting steps by date: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}