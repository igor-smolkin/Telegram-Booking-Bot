package com.ataraxii.testspringbot.db.service;

import com.ataraxii.testspringbot.db.entity.BotSettings;
import com.ataraxii.testspringbot.db.repository.BotSettingsRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotConfigService {

    private final BotSettingsRepository repository;
    private BotSettings cache;

    @Getter
    @Value("${google.table.credentials-path}")
    private String credentialsPath;

    public synchronized BotSettings get() {
        if (cache == null) {
            log.info("Загружаем BotSettings из базы данных...");
            cache = repository.findById(1L)
                    .orElseThrow(() -> new IllegalStateException(
                            "BotSettings record not found! Проверьте SQL-дефолт."));
            log.info("BotSettings загружены: {}", cache);
        }
        return cache;
    }

    public synchronized void update(Consumer<BotSettings> updater) {
        BotSettings settings = get();
        updater.accept(settings);
        repository.save(settings);
        cache = settings;
        log.info("BotSettings обновлены: {}", settings);
    }

    // --------------------------
    // Админские методы
    // --------------------------
    public synchronized void addAdminId(Long adminId) {
        if (adminId == null) {
            log.warn("Попытка добавить null в список админов — действие проигнорировано");
            return;
        }
        Set<String> admins = getAdminSet();
        if (admins.add(adminId.toString())) {
            update(s -> s.setAdminIds(String.join(",", admins)));
            log.info("Добавлен админ: {}", adminId);
        } else {
            log.info("Админ {} уже существует", adminId);
        }
    }

    public synchronized void removeAdminId(Long adminId) {
        if (adminId == null) {
            log.warn("Попытка удалить null из списка админов — действие проигнорировано");
            return;
        }
        Set<String> admins = getAdminSet();
        if (admins.remove(adminId.toString())) {
            update(s -> s.setAdminIds(String.join(",", admins)));
            log.info("Удалён админ: {}", adminId);
        } else {
            log.info("Админ {} не найден", adminId);
        }
    }

    public synchronized boolean isAdmin(Long userId) {
        return getAdminSet().contains(userId.toString());
    }

    private Set<String> getAdminSet() {
        String current = get().getAdminIds();
        if (current == null || current.isBlank()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(current.split(",")));
    }

    // --------------------------
    // Методы для Google
    // --------------------------
    public synchronized String getSpreadsheetId() {
        return get().getGoogleSpreadsheetId();
    }

    public synchronized String getCalendarId() {
        return get().getGoogleCalendarId();
    }

    public synchronized void setSpreadsheetId(String newId) {
        if (newId == null || newId.isBlank()) {
            log.warn("Попытка установить null или пустой Spreadsheet ID — действие проигнорировано");
            return;
        }
        update(s -> s.setGoogleSpreadsheetId(newId));
        log.info("Google Spreadsheet ID обновлён: {}", newId);
    }

    public synchronized void setCalendarId(String newId) {
        if (newId == null || newId.isBlank()) {
            log.warn("Попытка установить null или пустой Calendar ID — действие проигнорировано");
            return;
        }
        update(s -> s.setGoogleCalendarId(newId));
        log.info("Google Calendar ID обновлён: {}", newId);
    }
}
