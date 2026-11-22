package com.voltpulse.models;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseConnection {
    public static void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Try-with-resources to prevent resource leak warnings
                try (FileInputStream serviceAccount = new FileInputStream("E:/VoltPulseDesktop/VoltPulseDesktop/serviceAccountKey.json")) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl("https://voltpulsedesktop-default-rtdb.firebaseio.com/")
                            .build();

                    FirebaseApp.initializeApp(options);
                    System.out.println("Firebase Connected Successfully!");
                }
            }
        } catch (IOException e) {
            System.err.println("Firebase Connection Error: " + e.getMessage());
        }
    }
}