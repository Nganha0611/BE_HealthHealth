package com.nlu.Health.service;


import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private AuthRepository userRepository;

    public void saveUser(User user) {
             userRepository.save(user);

    }
    public User findUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }
//    [lg -5]
    public User getUsersByEmail(String email) {
//        [lg -8]
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByNumberPhoneAndIsVerify(String numberPhone) {
        return userRepository.findByNumberPhoneAndIsVerifyTrue(numberPhone);
    }
}
