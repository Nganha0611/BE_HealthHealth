package com.nlu.Health.controller;

import com.nlu.Health.model.User;
import com.nlu.Health.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

//    @PostMapping("/addUser")
//    public ResponseEntity<User> createUser(@RequestBody User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword())); // Mã hóa mật khẩu
//        User savedUser = userService.addUser(user);
//        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
//    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
        // Mã OTP đã được xác thực trước đó nên không cần kiểm tra email nữa
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user");
        userService.addUser(user);

        // Trả về JSON object có result
        Map<String, String> response = new HashMap<>();
        response.put("result", "success");
        response.put("message", "Đăng ký thành công!");

        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        List<User> foundUsers = userService.getUsersByEmail(user.getEmail());

        Map<String, String> response = new HashMap<>();

        if (foundUsers.isEmpty()) {
            response.put("message", "Email không tồn tại!");
            response.put("result", "false");
            return ResponseEntity.status(401).body(response);
        }

        User foundUser = foundUsers.get(0);
        if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            response.put("message", "Đăng nhập thành công!");
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Mật khẩu không chính xác!");
            response.put("result", "false");
            return ResponseEntity.status(401).body(response);
        }
    }
    @CrossOrigin(origins = "*")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String newPassword = payload.get("newPassword");

        List<User> foundUsers = userService.getUsersByEmail(email);
        Map<String, String> response = new HashMap<>();

        if (!foundUsers.isEmpty()) {
            User user = foundUsers.get(0);
            user.setPassword(passwordEncoder.encode(newPassword)); // Mã hóa mật khẩu trước khi lưu
            userService.addUser(user);

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
