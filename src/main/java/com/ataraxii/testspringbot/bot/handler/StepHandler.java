package com.ataraxii.testspringbot.bot.handler;

import com.ataraxii.testspringbot.bot.model.BookingStep;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

public interface StepHandler<T> {
    BookingStep getStep();

    BotApiMethod<?> handle(Long chatId, T text);

    default boolean supports(Object input) {
        return input == null || getGenericClass().isInstance(input);
    }

    Class<T> getGenericClass();
}
