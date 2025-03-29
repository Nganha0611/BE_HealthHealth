package com.nlu.Health.controller;

import com.nlu.Health.model.User;
import com.nlu.Health.repository.UserRepository;
import com.nlu.Health.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/addUser")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Mã hóa mật khẩu
        User savedUser = userService.addUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        List<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (!existingUser.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email đã tồn tại!");
            response.put("result", "false");

            return ResponseEntity.badRequest().body(response);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user");
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng ký thành công!");
        response.put("result", "success");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        List<User> foundUsers = userRepository.findByEmail(user.getEmail());

        Map<String, String> response = new HashMap<>();

        if (foundUsers.isEmpty()) {
            response.put("message", "Email hoặc mật khẩu không đúng!");
            response.put("result", "false");
            return ResponseEntity.status(401).body(response);
        }

        User foundUser = foundUsers.get(0);
        if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            response.put("message", "Đăng nhập thành công!");
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Email hoặc mật khẩu không đúng!");
            response.put("result", "false");
            return ResponseEntity.status(401).body(response);
        }
    }
    @CrossOrigin(origins = "*")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String newPassword = payload.get("newPassword");

        List<User> foundUsers = userRepository.findByEmail(email);
        Map<String, String> response = new HashMap<>();

        if (!foundUsers.isEmpty()) {
            User user = foundUsers.get(0);
            user.setPassword(passwordEncoder.encode(newPassword)); // Mã hóa mật khẩu trước khi lưu
            userRepository.save(user);

            response.put("message", "Mật khẩu đã được cập nhật!");
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Email không tồn tại!");
            response.put("result", "false");
            return ResponseEntity.status(404).body(response);
        }
    }

}
