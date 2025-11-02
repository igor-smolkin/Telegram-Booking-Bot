package com.ataraxii.testspringbot.bot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "google.table")
public class GoogleProperties {
    private String credentialsPath;
}
