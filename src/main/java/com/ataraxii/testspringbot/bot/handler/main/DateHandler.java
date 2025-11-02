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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class DateHandler implements StepHandler<String> {

    private final BookingStateService stateService;
    private final KeyboardFactory keyboardFactory;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public BookingStep getStep() {
        return BookingStep.DATE;
    }

    @Override
    public BotApiMethod<?> handle(Long chatId, String text) {
        if (text == null) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("Пожалуйста, введите дату посещения.")
                    .replyMarkup(keyboardFactory.durationKeyboard())
                    .build();
        }

        BookingData data = stateService.getData(chatId);

        if ("Назад к выбору".equals(text)) {
            BookingStep previousStep = stateService.goBack(chatId);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(keyboardFactory.getPromptForStep(previousStep))
                    .replyMarkup(keyboardFactory.getKeyboardForStep(previousStep))
                    .build();
        }

        try {
            LocalDate date = LocalDate.parse(text, FORMATTER);

            if (date.isBefore(LocalDate.now())) {
                throw new DateTimeParseException("Некорректная дата, попробуйте еще раз.", text, 0);
            }

            data.setDate(date);
            stateService.setStep(chatId, BookingStep.TIME);

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(keyboardFactory.getPromptForStep(BookingStep.TIME))
                    .replyMarkup(keyboardFactory.getKeyboardForStep(BookingStep.TIME))
                    .build();

        } catch (DateTimeParseException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Некорректный формат даты. Пожалуйста, используйте ДД.MM.ГГГГ или нажмите 'Назад к выбору'")
                    .replyMarkup(keyboardFactory.singleRowKeyboard("Назад к выбору"))
                    .build();
        }
    }

    @Override
    public Class<String> getGenericClass() {
        return String.class;
    }
}
