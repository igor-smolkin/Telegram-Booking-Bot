package com.ataraxii.testspringbot.bot.handler.main;

import com.ataraxii.testspringbot.bot.handler.StepHandler;
import com.ataraxii.testspringbot.bot.keyboard.KeyboardFactory;
import com.ataraxii.testspringbot.bot.model.BookingData;
import com.ataraxii.testspringbot.bot.model.BookingStep;
import com.ataraxii.testspringbot.bot.service.telegram.BookingStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DurationHandler implements StepHandler<String> {

    private final BookingStateService stateService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public BookingStep getStep() {
        return BookingStep.DURATION;
    }

    @Override
    public BotApiMethod<?> handle(Long chatId, String text) {

        if (text == null) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("Пожалуйста, выберите длительность посещения.")
                    .replyMarkup(keyboardFactory.durationKeyboard())
                    .build();
        }

        if ("Назад к выбору".equals(text)) {
            BookingStep previousStep = stateService.goBack(chatId);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(keyboardFactory.getPromptForStep(previousStep))
                    .replyMarkup(keyboardFactory.getKeyboardForStep(previousStep))
                    .build();
        }

        List<String> validDurations = List.of("30 минут", "1 час", "2 часа", "3 часа", "Безлимит");
        if (!validDurations.contains(text)) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Выберите корректную длительность посещения")
                    .replyMarkup(keyboardFactory.durationKeyboard())
                    .build();
        }

        BookingData data = stateService.getData(chatId);
        data.setDuration(text);
        stateService.setStep(chatId, BookingStep.PEOPLE_COUNT);

        return SendMessage.builder()
                .chatId(chatId)
                .text(keyboardFactory.getPromptForStep(BookingStep.PEOPLE_COUNT))
                .replyMarkup(keyboardFactory.getKeyboardForStep(BookingStep.PEOPLE_COUNT))
                .build();
    }

    @Override
    public Class<String> getGenericClass() {
        return String.class;
    }
}
