package com.amarpaybd.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {

            InputStream credentialsStream;

            // Render এ GOOGLE_CREDENTIALS environment variable থেকে নেবে
            String credentialsJson = System.getenv("GOOGLE_CREDENTIALS");
            if (credentialsJson != null && !credentialsJson.isEmpty()) {
                credentialsStream = new ByteArrayInputStream(
                    credentialsJson.getBytes(StandardCharsets.UTF_8)
                );
            } else {
                // Local development এ file থেকে নেবে
                credentialsStream = new FileInputStream(
                    "src/main/resources/service-account.json"
                );
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .setProjectId("sellpoint-ea3d2")
                .build();

            FirebaseApp.initializeApp(options);
        }
    }
}
