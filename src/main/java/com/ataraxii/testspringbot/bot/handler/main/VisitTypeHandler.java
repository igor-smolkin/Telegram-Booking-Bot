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

@Component
@RequiredArgsConstructor
public class VisitTypeHandler implements StepHandler<String> {

    private final BookingStateService stateService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public BookingStep getStep() {
        return BookingStep.VISIT_TYPE;
    }

    @Override
    public BotApiMethod<?> handle(Long chatId, String text) {

        if (text == null) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("Пожалуйста, выберите один из предложенных вариантов посещения.")
                    .replyMarkup(keyboardFactory.durationKeyboard())
                    .build();
        }

        BookingData data = stateService.getData(chatId);

        if (text.equals("Свободное посещение")) {
            data.setVisitType("Свободное посещение");
            stateService.setStep(chatId, BookingStep.DURATION);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(keyboardFactory.getPromptForStep(BookingStep.VISIT_TYPE))
                    .replyMarkup(keyboardFactory.durationKeyboard())
                    .build();
        } else if (text.equals("Индивидуальная тренировка")) {
            data.setVisitType("Индивидуальная тренировка");
            stateService.setStep(chatId, BookingStep.DATE);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Введите дату тренировки в формате ДД.ММ.ГГГГ (например: 29.10.2025)")
                    .replyMarkup(keyboardFactory.durationKeyboard())
                    .build();
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text("Пожалуйста, выберите один из предложенных вариантов")
                .replyMarkup(keyboardFactory.visitTypeKeyboard())
                .build();
    }

    @Override
    public Class<String> getGenericClass() {
        return String.class;
    }
}
