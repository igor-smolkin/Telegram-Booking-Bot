package com.ataraxii.testspringbot.handler.main;

import com.ataraxii.testspringbot.handler.StepHandler;
import com.ataraxii.testspringbot.keyboard.KeyboardFactory;
import com.ataraxii.testspringbot.model.BookingData;
import com.ataraxii.testspringbot.model.BookingStep;
import com.ataraxii.testspringbot.service.telegram.BookingStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class PeopleCountHandler implements StepHandler<String> {

    private final BookingStateService stateService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public BookingStep getStep() {
        return BookingStep.PEOPLE_COUNT;
    }

    @Override
    public BotApiMethod<?> handle(Long chatId, String text) {

        if (text == null) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("Пожалуйста, введите количество человек планирующих посещение.")
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

        try {
            int count = Integer.parseInt(text.trim());

            if (count <= 0) {
                return SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("Количество человек должно быть положительным числом. Попробуйте снова.")
                        .replyMarkup(keyboardFactory.singleRowKeyboard("Назад к выбору"))
                        .build();
            }

            if (count > 40) {
                return SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("Максимальное количество человек за одно посещение — 40. Пожалуйста, введите меньшее число.")
                        .replyMarkup(keyboardFactory.singleRowKeyboard("Назад к выбору"))
                        .build();
            }

            BookingData data = stateService.getData(chatId);
            data.setPeopleCount(count);
            stateService.setStep(chatId, BookingStep.DATE);

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(keyboardFactory.getPromptForStep(BookingStep.DATE))
                    .replyMarkup(keyboardFactory.getKeyboardForStep(BookingStep.DATE))
                    .build();

        } catch (NumberFormatException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Пожалуйста, введите количество числом (например: 4) или нажмите 'Назад к выбору'")
                    .replyMarkup(keyboardFactory.singleRowKeyboard("Назад к выбору"))
                    .build();
        }
    }

    @Override
    public Class<String> getGenericClass() {
        return String.class;
    }
}
