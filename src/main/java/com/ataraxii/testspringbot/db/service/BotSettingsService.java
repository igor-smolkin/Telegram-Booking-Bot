package com.ataraxii.testspringbot.db.service;

import com.ataraxii.testspringbot.db.entity.BotSettings;
import com.ataraxii.testspringbot.db.repository.BotSettingsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class BotSettingsService {

    private final BotSettingsRepository repository;
    private BotSettings cache;

    @PostConstruct
    public void init() {
        cache = repository.findById(1L)
                .orElseGet(() -> repository.save(new BotSettings()));
    }

    public BotSettings get() {
        return cache;
    }

    public void update(Consumer<BotSettings> updater) {
        updater.accept(cache);
        repository.save(cache);
    }

    public String getGoogleSpreadsheetId() {
        return cache.getGoogleSpreadsheetId();
    }

    public String getGoogleCalendarId() {
        return cache.getGoogleCalendarId();
    }

    public Long getAdminGroupChatId() {
        return cache.getAdminGroupChatId();
    }

    public String getAdminIds() {
        return cache.getAdminIds();
    }
}
