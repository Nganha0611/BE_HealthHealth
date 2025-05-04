package com.nlu.Health.repository;

import com.nlu.Health.model.BloodPressure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodPressureRepository extends MongoRepository<BloodPressure, String> {
    BloodPressure findFirstByUserIdOrderByCreatedAtDesc(String userId);

    List<BloodPressure> findByUserIdOrderByCreatedAtAsc(String userId);
}
