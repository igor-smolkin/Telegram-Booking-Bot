package com.ataraxii.testspringbot.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bot_settings")
@Builder
public class BotSettings {

    @Id
    private Long id;

    private Long adminGroupChatId;

    @Column(length = 1000)
    private String adminIds;

    private String googleSpreadsheetId;

    private String googleCalendarId;
}
