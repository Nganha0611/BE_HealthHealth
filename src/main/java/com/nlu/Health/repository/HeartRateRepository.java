package com.nlu.Health.repository;

import com.nlu.Health.model.HeartRate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HeartRateRepository extends MongoRepository<HeartRate, String> {
    HeartRate findFirstByUserIdOrderByCreatedAtDesc(String userId);
    Optional<HeartRate> findByUserIdAndCreatedAtAndHeartRate(String userId, Date createdAt, double heartRate);
    List<HeartRate> findByUserIdOrderByCreatedAtAsc(String userId);
}

