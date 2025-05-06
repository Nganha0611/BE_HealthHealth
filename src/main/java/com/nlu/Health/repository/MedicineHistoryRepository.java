package com.nlu.Health.repository;

import com.nlu.Health.model.MedicineHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineHistoryRepository extends MongoRepository<MedicineHistory, String> {
    List<MedicineHistory> findByUserIdOrderByTimestampDesc(String userId);
    List<MedicineHistory> findByPrescriptionsId(String prescriptionsId);
    MedicineHistory findByIdAndUserId(String id, String userId);
}
