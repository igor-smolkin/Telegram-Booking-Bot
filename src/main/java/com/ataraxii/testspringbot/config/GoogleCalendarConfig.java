package com.ataraxii.testspringbot.config;

import com.ataraxii.testspringbot.properties.GoogleProperties;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    private final GoogleProperties properties;

    public GoogleCalendarConfig(GoogleProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Calendar calendarService() throws GeneralSecurityException, IOException {
        InputStream credentialsStream = getClass().getClassLoader()
                .getResourceAsStream(properties.getCredentialsPath());

        if (credentialsStream == null) {
            throw new RuntimeException(
                    "Не удалось найти credentials.json в classpath по пути: " + properties.getCredentialsPath()
            );
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/calendar"));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("BookingBot")
                .build();
    }
}
