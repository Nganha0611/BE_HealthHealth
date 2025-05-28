package com.nlu.Health.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tracking_permissions")
public class TrackingPermission {
    @Id
    private String id;
    private String followerUserId;
    private String followedUserId;
    private String status;
    private String timestamp;

    // Constructors
    public TrackingPermission() {}

    public TrackingPermission(String followerUserId, String followedUserId, String status, String timestamp) {
        this.followerUserId = followerUserId;
        this.followedUserId = followedUserId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters và Setters
    public String getId() { return id; }
    public String getFollowerUserId() { return followerUserId; }
    public void setFollowerUserId(String followerUserId) { this.followerUserId = followerUserId; }
    public String getFollowedUserId() { return followedUserId; }
    public void setFollowedUserId(String followedUserId) { this.followedUserId = followedUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}