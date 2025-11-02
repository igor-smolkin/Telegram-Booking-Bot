package com.ataraxii.testspringbot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "telegram.admin")
public class TelegramAdminProperties {
    List<Long> ids;
}
