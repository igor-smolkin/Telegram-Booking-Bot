package com.ataraxii.testspringbot.handler.admin;

import com.ataraxii.testspringbot.handler.StepHandler;
import com.ataraxii.testspringbot.model.BookingStep;
import com.ataraxii.testspringbot.properties.GoogleProperties;
import com.ataraxii.testspringbot.properties.TelegramBotProperties;
import com.ataraxii.testspringbot.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCommandHandler implements StepHandler<String> {

    private final AdminService adminService;
    private final TelegramBotProperties botProperties;
    private final GoogleProperties googleProperties;

    @Override
    public BookingStep getStep() {
        return null;
    }

    @Override
    public BotApiMethod<?> handle(Long chatId, String text) {
        return null;
    }

    @Override
    public Class<String> getGenericClass() {
        return null;
    }
}
