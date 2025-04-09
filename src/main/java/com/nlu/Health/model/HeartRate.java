package com.nlu.Health.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Document(collection = "heart_rates")
public class HeartRate {
    @Id
    private String id;
    private String userId;
    private int heartRate;
    private Date createdAt;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public int getHeartRate() {
        return heartRate;
    }



    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

