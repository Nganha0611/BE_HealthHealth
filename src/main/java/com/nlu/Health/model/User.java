package com.nlu.Health.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String birth;
    private String sex;
    private String numberPhone;
    private String address;
    private String role;
    private String url;
    private boolean isVerify;
    private String fcmToken; // Thêm trường để lưu FCM Token

    // Constructors
    public User() {}

    // Getters và Setters cho các trường mới
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    // Các getters và setters hiện có
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBirth() { return birth; }
    public void setBirth(String birth) { this.birth = birth; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getNumberPhone() { return numberPhone; }
    public void setNumberPhone(String numberPhone) { this.numberPhone = numberPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isVerify() { return isVerify; }
    public void setVerify(boolean verify) { isVerify = verify; }
}