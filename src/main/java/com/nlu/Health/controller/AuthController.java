package com.nlu.Health.controller;

import com.nlu.Health.model.User;
import com.nlu.Health.repository.AuthRepository;
import com.nlu.Health.response.UserResponse;
import com.nlu.Health.service.AuthService;
import com.nlu.Health.tools.JwtUtil;
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
    @Autowired
    private AuthRepository authRepository;

    //    @PostMapping("/addUser")
//    public ResponseEntity<User> createUser(@RequestBody User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword())); // Mã hóa mật khẩu
//        User savedUser = userService.addUser(user);
//        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
//    }
@GetMapping("/info")
public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String tokenHeader) {
    String token = tokenHeader.replace("Bearer ", "");
    String email = JwtUtil.validateToken(token); // Lấy email từ token

    if (email == null) {
        return ResponseEntity.status(401).body("Token không hợp lệ hoặc hết hạn");
    }

    User user = authRepository.findByEmail(email);
    if (user == null) {
        return ResponseEntity.status(404).body("Không tìm thấy người dùng");
    }

    // Không trả password
    Map<String, Object> response = new HashMap<>();
    response.put("id", user.getId());
    response.put("name", user.getName());
    response.put("email", user.getEmail());
    response.put("birth", user.getBirth());
    response.put("sex", user.getSex());
    response.put("numberPhone", user.getNumberPhone());
    response.put("address", user.getAddress());
    response.put("role", user.getRole());
    response.put("url", user.getUrl());

    return ResponseEntity.ok(response);
}

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
        User foundUser = userService.getUsersByEmail(user.getEmail());
        Map<String, Object> response = new HashMap<>();

        if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            String token = JwtUtil.generateToken(foundUser.getEmail());

            response.put("message", "Đăng nhập thành công!");
            response.put("result", "success");
            response.put("token", token);
            response.put("user", new UserResponse(foundUser)); // Không trả password

            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Mật khẩu không chính xác!");
            response.put("result", "wrongPassword");
            return ResponseEntity.status(401).body(response);
        }

    }

    @CrossOrigin(origins = "*")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String newPassword = payload.get("newPassword");

        User user = userService.getUsersByEmail(email);
        Map<String, String> response = new HashMap<>();

        if (!(user == null)) {
            user.setPassword(passwordEncoder.encode(newPassword));
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
