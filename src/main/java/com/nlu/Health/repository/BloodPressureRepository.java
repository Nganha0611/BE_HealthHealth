package com.nlu.Health.repository;

import com.nlu.Health.model.BloodPressure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodPressureRepository extends MongoRepository<BloodPressure, String> {
}
