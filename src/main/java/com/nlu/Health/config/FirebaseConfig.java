package com.nlu.Health.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void init() {
        try {
            // Thử cả hai đường dẫn để tìm secret file
            String serviceAccountPath = new File("/serviceAccountKey.json").exists()
                    ? "/serviceAccountKey.json"
                    : "/etc/secrets/serviceAccountKey.json";
            logger.info("Đường dẫn secret file: {}", serviceAccountPath);

            File file = new File(serviceAccountPath);
            if (!file.exists()) {
                logger.error("Tệp {} không tồn tại.", serviceAccountPath);
                throw new IOException("Tệp serviceAccountKey.json không tồn tại.");
            }

            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase App khởi tạo thành công.");
            } else {
                logger.info("Firebase App đã được khởi tạo trước đó.");
            }
        } catch (IOException e) {
            logger.error("Khởi tạo Firebase App thất bại: {}", e.getMessage(), e);
            throw new RuntimeException("Khởi tạo Firebase thất bại.", e);
        }
    }
}
