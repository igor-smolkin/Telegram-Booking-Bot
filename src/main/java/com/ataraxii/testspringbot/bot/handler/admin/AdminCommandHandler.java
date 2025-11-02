package com.ataraxii.testspringbot.bot.handler.admin;

import com.ataraxii.testspringbot.db.service.BotConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCommandHandler {

    private final BotConfigService configService;

    public BotApiMethod<?> handle(Long chatId, String text) {
        // Проверка на права администратора
        if (!configService.isAdmin(chatId)) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ У вас нет прав для выполнения этой команды.")
                    .build();
        }

        if (text == null || text.isBlank()) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Пожалуйста, укажите команду.")
                    .build();
        }

        String[] parts = text.trim().split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "/addadmin" -> {
                    Long newAdminId = Long.parseLong(parts[1]);
                    configService.addAdminId(newAdminId);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("✅ Добавлен новый админ: " + newAdminId)
                            .build();
                }

                case "/removeadmin" -> {
                    Long removeAdminId = Long.parseLong(parts[1]);
                    configService.removeAdminId(removeAdminId);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("✅ Удалён админ: " + removeAdminId)
                            .build();
                }

                case "/setadmingroupid" -> {
                    Long newGroupId = Long.parseLong(parts[1]);
                    configService.update(s -> s.setAdminGroupChatId(newGroupId));
                    log.info("Admin group chat ID изменён на {}", newGroupId);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("✅ Изменён чат для админов: " + newGroupId)
                            .build();
                }

                case "/setspreadsheetid" -> {
                    String newId = parts[1];
                    configService.setSpreadsheetId(newId);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("✅ Изменён SpreadsheetId: " + newId)
                            .build();
                }

                case "/setcalendarid" -> {
                    String newId = parts[1];
                    configService.setCalendarId(newId);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("✅ Изменён CalendarId: " + newId)
                            .build();
                }

                default -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("❌ Неизвестная команда.")
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении админ-команды: {}", text, e);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Ошибка при выполнении команды: " + e.getMessage())
                    .build();
        }
    }
}
