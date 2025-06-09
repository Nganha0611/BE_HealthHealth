package com.nlu.Health.repository;

import com.nlu.Health.model.TrackingPermission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Thêm import này

@Repository
public interface TrackingPermissionRepository extends MongoRepository<TrackingPermission, String> {
    List<TrackingPermission> findByFollowerUserId(String followerUserId);
    List<TrackingPermission> findByFollowedUserId(String followedUserId);
    Optional<TrackingPermission> findByFollowerUserIdAndFollowedUserId(String followerUserId, String followedUserId);
    Optional<TrackingPermission> findByFollowerUserIdAndFollowedUserIdAndStatus(String followerUserId, String followedUserId, String status);
    List<TrackingPermission> findByFollowedUserIdAndStatus(String followedUserId, String status);
}