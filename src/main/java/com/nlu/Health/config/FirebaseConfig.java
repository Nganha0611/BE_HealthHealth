// package com.nlu.Health.config;

// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.firebase.FirebaseApp;
// import com.google.firebase.FirebaseOptions;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.context.annotation.Configuration;

// import jakarta.annotation.PostConstruct;
// import java.io.ByteArrayInputStream;
// import java.io.FileNotFoundException;

// @Configuration
// public class FirebaseConfig {
//     private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

//     @PostConstruct
//     public void init() {
//         try {
//             // Lấy nội dung JSON từ biến môi trường FIREBASE_CONFIG
//             String firebaseConfig = System.getenv("FIREBASE_CONFIG");
//             if (firebaseConfig == null || firebaseConfig.trim().isEmpty()) {
//                 throw new FileNotFoundException("Firebase config not found in environment variable 'FIREBASE_CONFIG'");
//             }

//             // Chuyển nội dung JSON thành stream để khởi tạo Firebase
//             GoogleCredentials credentials = GoogleCredentials.fromStream(
//                     new ByteArrayInputStream(firebaseConfig.getBytes()));
//             FirebaseOptions options = FirebaseOptions.builder()
//                     .setCredentials(credentials)
//                     .build();

//             // Khởi tạo FirebaseApp nếu chưa có
//             if (FirebaseApp.getApps().isEmpty()) {
//                 FirebaseApp.initializeApp(options);
//                 logger.info("Firebase App initialized successfully.");
//             } else {
//                 logger.info("Firebase App already initialized.");
//             }
//         } catch (Exception e) {
//             logger.error("Failed to initialize Firebase App: {}", e.getMessage(), e);
//             throw new RuntimeException("Firebase initialization failed. Check environment variable 'FIREBASE_CONFIG'.", e);
//         }
//     }
// }
