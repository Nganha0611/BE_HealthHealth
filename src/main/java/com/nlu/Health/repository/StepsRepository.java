package com.nlu.Health.repository;

import com.nlu.Health.model.Steps;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface StepsRepository extends MongoRepository<Steps, String> {
    Steps findFirstByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Steps> findFirstByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(String userId, Date startDate, Date endDate);
    List<Steps> findByUserIdOrderByCreatedAtAsc(String userId);
    int deleteByUserIdAndCreatedAtBetween(String userId, Date startDate, Date endDate);
}