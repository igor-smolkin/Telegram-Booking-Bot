package com.ataraxii.testspringbot.handler;

import com.ataraxii.testspringbot.model.BookingStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingHandler {

    private final Map<BookingStep, StepHandler<?>> handlerMap;

    public BookingHandler(List<StepHandler<?>> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(StepHandler::getStep, h -> h));
    }

    public BotApiMethod<?> handle(Long chatId, BookingStep step, Object input) {

        StepHandler<?> rawHandler = handlerMap.get(step);

        if (rawHandler == null) {
            log.error("❌ Нет зарегистрированного обработчика для шага: {}", step);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Нет обработчика для текущего шага")
                    .build();
        }

        try {
            return handleStep(rawHandler, chatId, input);
        } catch (ClassCastException e) {
            log.error("Ошибка приведения типа для шага {}", step, e);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Ошибка обработки шага. Некорректный тип данных.")
                    .build();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке шага {}", step, e);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Произошла ошибка при обработке шага. Попробуйте снова.")
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> BotApiMethod<?> handleStep(StepHandler<T> handler, Long chatId, Object input) {
        return handler.handle(chatId, (T) input);
    }

    public StepHandler<?> getHandler(BookingStep step) {
        return handlerMap.get(step);
    }
}
