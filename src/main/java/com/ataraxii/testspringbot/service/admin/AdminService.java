package com.ataraxii.testspringbot.service.admin;

import com.ataraxii.testspringbot.properties.TelegramAdminProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Service
public class AdminService {

    private final Set<Long> adminIds = new HashSet<>();

    public AdminService(TelegramAdminProperties properties) {
        if (properties.getIds() != null) {
            adminIds.addAll(properties.getIds());
        }
    }

    /**
     * Проверяет, является ли пользователь админом
     */
    public boolean isAdmin(Long userId) {
        return adminIds.contains(userId);
    }

    /**
     * Динамически добавляет админа
     */
    public void addAdmin(Long userId) {
        adminIds.add(userId);
    }

    /**
     * Динамически удаляет админа
     */
    public void removeAdmin(Long userId) {
        adminIds.remove(userId);
    }

    /**
     * Возвращает неизменяемый список текущих админов
     */
    public Set<Long> getAdminIds() {
        return Collections.unmodifiableSet(adminIds);
    }
}
