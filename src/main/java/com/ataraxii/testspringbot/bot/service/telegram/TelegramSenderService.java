package com.ataraxii.testspringbot.bot.service.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class TelegramSenderService {

    private final TelegramBotExecutor botExecutor;

    public TelegramSenderService(@Lazy TelegramBotExecutor botExecutor) {
        this.botExecutor = botExecutor;
    }

    public void sendMessage(SendMessage message) {
        try {
            botExecutor.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения в Telegram: {}", message, e);
        }
    }

    public void editMessage(EditMessageText editMessage) {
        try {
            botExecutor.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при редактировании сообщения в Telegram: {}", editMessage, e);
        }
    }
}
