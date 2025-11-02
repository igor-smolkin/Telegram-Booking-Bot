package com.ataraxii.testspringbot.bot.handler;

import com.ataraxii.testspringbot.bot.model.BookingStep;
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

    public StepHandler<?> getHandler(BookingStep step) {
        return handlerMap.get(step);
    }
}
