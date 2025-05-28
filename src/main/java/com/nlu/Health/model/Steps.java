package com.nlu.Health.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "steps")
public class Steps {
    @Id
    private String id;
    private String userId;
    private int steps;
    private Date createdAt;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public int getSteps() {
        return steps;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}