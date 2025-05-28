package com.nlu.Health.repository;

import com.nlu.Health.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
    User findByNumberPhone(String numberPhone);
    Optional<User> findByNumberPhoneAndIsVerifyTrue(String numberPhone);
    User findByFcmToken(String fcmToken); // Thêm phương thức mới
    Optional<User> findById(String id);}