package com.nlu.Health.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "prescriptions")
public class Prescription {
    @Id
    private String id;
    private String userId;
    private String name;
    private String form;
    private String strength;
    private String unit;
    private String amount;
    private String instruction;
    private String startday;
    private RepeatDetails repeatDetails;

    // Nested class for repeat details
    public static class RepeatDetails {
        private String type;
        private String interval;
        private List<String> daysOfWeek;
        private List<String> daysOfMonth;
        private List<String> timePerDay;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public List<String> getDaysOfWeek() {
            return daysOfWeek;
        }

        public void setDaysOfWeek(List<String> daysOfWeek) {
            this.daysOfWeek = daysOfWeek;
        }

        public List<String> getDaysOfMonth() {
            return daysOfMonth;
        }

        public void setDaysOfMonth(List<String> daysOfMonth) {
            this.daysOfMonth = daysOfMonth;
        }

        public List<String> getTimePerDay() {
            return timePerDay;
        }

        public void setTimePerDay(List<String> timePerDay) {
            this.timePerDay = timePerDay;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getStartday() {
        return startday;
    }

    public void setStartday(String startday) {
        this.startday = startday;
    }

    public RepeatDetails getRepeatDetails() {
        return repeatDetails;
    }

    public void setRepeatDetails(RepeatDetails repeatDetails) {
        this.repeatDetails = repeatDetails;
    }
}