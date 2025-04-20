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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user");
        userService.addUser(user);
        Map<String, String> response = new HashMap<>();
        response.put("result", "success");
        response.put("message", "Đăng ký thành công!");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        User foundUser = userService.getUsersByEmail(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        if (foundUser == null) {
            response.put("message", "Email không tồn tại!");
            response.put("result", "emailNotExist");
            return ResponseEntity.status(404).body(response);
        }
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
            response.put("result", "emailNotExist");
            return ResponseEntity.status(404).body(response);
        }
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/update-info")
    public ResponseEntity<Map<String, String>> updateUserInfo(@RequestBody Map<String, String> payload) {
        String currentEmail = payload.get("currentEmail"); // email hiện tại để tìm user

        Map<String, String> response = new HashMap<>();
        User user = userService.getUsersByEmail(currentEmail);

        if (user != null) {
            // Cập nhật thông tin nếu có truyền vào
            if (payload.containsKey("name")) {
                user.setName(payload.get("name"));
            }
            if (payload.containsKey("email")) {
                user.setEmail(payload.get("email"));
            }
            if (payload.containsKey("numberPhone")) {
                user.setNumberPhone(payload.get("numberPhone"));
            }
            if (payload.containsKey("address")) {
                user.setAddress(payload.get("address"));
            }

            userService.addUser(user); // Lưu lại thay đổi

            response.put("result", "success");
            response.put("message", "Thông tin người dùng đã được cập nhật!");
            return ResponseEntity.ok(response);
        } else {
            response.put("result", "emailNotExist");
            response.put("message", "Email không tồn tại!");
            return ResponseEntity.status(404).body(response);
        }
    }
    @CrossOrigin(origins = "*")
    @PutMapping("/update-profile-image")
    public ResponseEntity<Map<String, String>> updateProfileImage(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String url = payload.get("url"); // sử dụng trường url từ class User

        Map<String, String> response = new HashMap<>();
        User user = userService.getUsersByEmail(email);

        if (user != null) {
            user.setUrl(url);
            userService.addUser(user);

            response.put("result", "success");
            response.put("message", "Ảnh đại diện đã được cập nhật!");
            return ResponseEntity.ok(response);
        } else {
            response.put("result", "emailNotExist");
            response.put("message", "Email không tồn tại!");
            return ResponseEntity.status(404).body(response);
        }
    }

}
