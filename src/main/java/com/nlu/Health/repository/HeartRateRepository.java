package com.nlu.Health.repository;

import com.nlu.Health.model.HeartRate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeartRateRepository extends MongoRepository<HeartRate, String> {
    HeartRate findFirstByOrderByCreatedAtDesc();

    HeartRate findFirstByUserIdOrderByCreatedAtDesc(String userId);

    List<HeartRate> findByUserIdOrderByCreatedAtAsc(String userId);
}

