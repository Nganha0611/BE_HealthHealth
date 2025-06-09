package com.nlu.Health.repository;

import com.nlu.Health.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends MongoRepository<User, String> {
//    [lg-6-7]
    User findByEmail(String email);
    Optional<User> findByNumberPhoneAndIsVerifyTrue(String numberPhone);
    Optional<User> findById(String id);}