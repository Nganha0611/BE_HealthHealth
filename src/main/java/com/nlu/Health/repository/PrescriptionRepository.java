package com.nlu.Health.repository;

import com.nlu.Health.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByUserIdOrderByNameAsc(String userId);
    Prescription findByIdAndUserId(String id, String userId);
    List<Prescription> findByUserIdAndNameContainingIgnoreCase(String userId, String name);
}