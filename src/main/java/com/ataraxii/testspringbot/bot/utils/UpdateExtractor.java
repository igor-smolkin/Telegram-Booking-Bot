package com.ataraxii.testspringbot.bot.utils;

import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateExtractor {
    /** Получаем chatId безопасно */
    public static Long getChatId(Update update) {
        if (update == null) return null;

        if (update.hasMessage()) return update.getMessage().getChatId();
        if (update.hasCallbackQuery() && update.getCallbackQuery().getMessage() != null) {
            return update.getCallbackQuery().getMessage().getChatId();
        }

        return null;
    }

    /** Получаем текстовое сообщение безопасно */
    public static String getText(Update update) {
        if (update == null) return null;

        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().getText();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        }

        return null;
    }

    /** Получаем контакт, если пользователь его прислал */
    public static Contact getContact(Update update) {
        if (update == null) return null;

        if (update.hasMessage() && update.getMessage().hasContact()) {
            return update.getMessage().getContact();
        }

        return null;
    }

    /** Проверка, что апдейт поддерживаемый (текст, контакт или callback) */
    public static boolean isSupported(Update update) {
        return getText(update) != null || getContact(update) != null;
    }
}
