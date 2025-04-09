package com.nlu.Health.controller;

import com.nlu.Health.model.BloodPressure;
import com.nlu.Health.model.HeartRate;
import com.nlu.Health.repository.BloodPressureRepository;
import com.nlu.Health.repository.HeartRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/blood-pressures")
public class BloodPressureController {

    @Autowired
    private BloodPressureRepository bloodPressRepo;

    @PostMapping("/measure")
    public ResponseEntity<BloodPressure> createBloodPressure(@RequestBody BloodPressure bloodPressure) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 7); // Thêm 7 giờ
        Date currentDate = calendar.getTime();

        bloodPressure.setCreatedAt(currentDate);
        return ResponseEntity.ok(bloodPressRepo.save(bloodPressure));
    }

}
