package com.nlu.Health.repository;

import com.nlu.Health.model.EmergencyContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends MongoRepository<EmergencyContact, String> {
    List<EmergencyContact> findByUserId(String userId);
    List<EmergencyContact> findByUserIdAndPhoneNumber(String userId, String phoneNumber);
}