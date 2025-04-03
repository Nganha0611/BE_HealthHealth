package com.nlu.Health.repository;

import com.nlu.Health.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthRepository extends MongoRepository<User, String> {
    List<User> findByEmail(String email);
}