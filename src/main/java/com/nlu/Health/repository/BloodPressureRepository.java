package com.nlu.Health.repository;

import com.nlu.Health.model.BloodPressure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodPressureRepository extends MongoRepository<BloodPressure, String> {
    BloodPressure findFirstByUserIdOrderByCreatedAtDesc(String userId);
    Optional<BloodPressure> findByUserIdAndCreatedAtAndSystolicAndDiastolic(String userId, Date createdAt, double systolic, double diastolic);
    List<BloodPressure> findByUserIdOrderByCreatedAtAsc(String userId);
}
