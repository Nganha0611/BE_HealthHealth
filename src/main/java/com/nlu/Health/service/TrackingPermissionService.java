package com.nlu.Health.service;

import com.nlu.Health.model.TrackingPermission;
import com.nlu.Health.repository.TrackingPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TrackingPermissionService {
    @Autowired
    private TrackingPermissionRepository trackingPermissionRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public TrackingPermission createTrackingPermission(String followerUserId, String followedUserId) {
        Optional<TrackingPermission> existingPermission = trackingPermissionRepository
                .findByFollowerUserIdAndFollowedUserId(followerUserId, followedUserId);

        if (existingPermission.isPresent()) {
            TrackingPermission permission = existingPermission.get();
            if ("canceled".equals(permission.getStatus()) || "rejected".equals(permission.getStatus())) {
                permission.setStatus("pending");
                permission.setTimestamp(DATE_FORMAT.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())));
                return trackingPermissionRepository.save(permission);
            }
            throw new IllegalStateException("A tracking request already exists with status: " + permission.getStatus());
        }

        TrackingPermission permission = new TrackingPermission(followerUserId, followedUserId, "pending",
                DATE_FORMAT.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())));
        return trackingPermissionRepository.save(permission);
    }

    public TrackingPermission updateTrackingPermissionStatus(String id, String status) {
        Optional<TrackingPermission> permissionOpt = trackingPermissionRepository.findById(id);
        if (permissionOpt.isPresent()) {
            TrackingPermission permission = permissionOpt.get();
            permission.setStatus(status);
            permission.setTimestamp(DATE_FORMAT.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())));
            return trackingPermissionRepository.save(permission);
        }
        return null;
    }

    public List<TrackingPermission> getPermissionsByUserId(String userId, boolean isFollower) {
        return isFollower
                ? trackingPermissionRepository.findByFollowerUserId(userId)
                : trackingPermissionRepository.findByFollowedUserId(userId);
    }

    public void deleteTrackingPermission(String id) {
        trackingPermissionRepository.deleteById(id);
    }

    public TrackingPermission findByFollowerAndFollowed(String followerId, String followedId) {
        Optional<TrackingPermission> permission = trackingPermissionRepository
                .findByFollowerUserIdAndFollowedUserIdAndStatus(followerId, followedId, "approved");
        return permission.orElse(null);
    }

    public TrackingPermission findById(String id) {
        return trackingPermissionRepository.findById(id).orElse(null);
    }
}