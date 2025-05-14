package com.nlu.Health.repository;

import com.nlu.Health.model.MedicalHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalHistoryRepository extends MongoRepository<MedicalHistory, String> {
    List<MedicalHistory> findByUserIdOrderByTimestampDesc(String userId);
    MedicalHistory findByIdAndUserId(String id, String userId);
}
