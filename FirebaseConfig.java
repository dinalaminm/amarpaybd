package com.amarpaybd.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // service-account.json ফাইলটা resources/ ফোল্ডারে রাখো
            FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/service-account.json");

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId("sellpoint-ea3d2")  // তোমার project ID
                .build();

            FirebaseApp.initializeApp(options);
        }
    }
}
