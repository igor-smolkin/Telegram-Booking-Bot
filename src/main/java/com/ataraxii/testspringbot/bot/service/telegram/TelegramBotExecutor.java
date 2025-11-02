package com.ataraxii.testspringbot.bot.service.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramBotExecutor {
    void execute(SendMessage message) throws TelegramApiException;
    void execute(EditMessageText message) throws TelegramApiException;
}
