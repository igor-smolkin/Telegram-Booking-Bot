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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class TimeHandler implements StepHandler<String> {

    private final BookingStateService stateService;
    private final KeyboardFactory keyboardFactory;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public BookingStep getStep() {
        return BookingStep.TIME;
    }

    @Override
    public BotApiMethod<?> handle(Long chatId, String text) {
        if (text == null) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("Пожалуйста, введите время посещения.")
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
            LocalTime time = LocalTime.parse(text, FORMATTER);

            // Проверяем дату (должна быть уже выбрана на предыдущем шаге)
            LocalDate date = data.getDate();
            if (date == null) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Сначала выберите дату посещения.")
                        .replyMarkup(keyboardFactory.getKeyboardForStep(BookingStep.DATE))
                        .build();
            }

            // Определяем время работы в зависимости от дня недели
            LocalTime openTime;
            LocalTime closeTime;
            if (isWeekend(date)) {
                openTime = LocalTime.of(10, 0);
                closeTime = LocalTime.of(22, 0);
            } else {
                openTime = LocalTime.of(12, 0);
                closeTime = LocalTime.of(22, 0);
            }

            // Проверяем, входит ли введённое время в рабочие часы
            if (time.isBefore(openTime) || time.isAfter(closeTime)) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text(String.format(
                                "Введено некорректное время посещения.\n" +
                                        "В этот день доступно с %s до %s.",
                                openTime, closeTime))
                        .replyMarkup(keyboardFactory.singleRowKeyboard("Назад к выбору"))
                        .build();
            }

            data.setTime(time);
            stateService.setStep(chatId, BookingStep.CONTACT_CONFIRM);

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(keyboardFactory.getPromptForStep(BookingStep.CONTACT_CONFIRM))
                    .replyMarkup(keyboardFactory.getKeyboardForStep(BookingStep.CONTACT_CONFIRM))
                    .build();
        } catch (DateTimeParseException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Некорректный формат времени. Пожалуйста, используйте ЧЧ:ММ")
                    .replyMarkup(keyboardFactory.singleRowKeyboard("Назад к выбору"))
                    .build();
        }
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    @Override
    public Class<String> getGenericClass() {
        return String.class;
    }
}
