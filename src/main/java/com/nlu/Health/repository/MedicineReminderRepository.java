package com.nlu.Health.repository;

import com.nlu.Health.model.MedicineReminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MedicineReminderRepository extends MongoRepository<MedicineReminder, String> {
    List<MedicineReminder> findByStatusAndScheduledTimeBetween(String status, Date start, Date end);
    MedicineReminder findByUserIdAndScheduledTime(String userId, Date scheduledTime);

    List<MedicineReminder> findByUserIdAndPrescriptionIdAndScheduledTime(String userId, String id, Date scheduledTime);
}