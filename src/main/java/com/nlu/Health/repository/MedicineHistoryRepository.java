package com.nlu.Health.repository;

import com.nlu.Health.model.MedicineHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MedicineHistoryRepository extends MongoRepository<MedicineHistory, String> {
    List<MedicineHistory> findByUserIdOrderByTimestampDesc(String userId);
    MedicineHistory findByIdAndUserId(String id, String userId);
}