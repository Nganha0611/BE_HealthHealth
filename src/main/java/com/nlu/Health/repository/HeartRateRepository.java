package com.nlu.Health.repository;

import com.nlu.Health.model.HeartRate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartRateRepository extends MongoRepository<HeartRate, String> {}

