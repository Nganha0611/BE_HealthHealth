package com.nlu.Health.model;

public class OtpData {
    private final String otp;
    private final long expiresAt;

    public OtpData(String otp, long expiresAt) {
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public String getOtp() {
        return otp;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}