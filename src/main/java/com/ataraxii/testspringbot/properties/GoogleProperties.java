package com.ataraxii.testspringbot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "google.table")
public class GoogleProperties {
    private String spreadsheetId;
    private String credentialsPath;
    private String calendarId;
}
