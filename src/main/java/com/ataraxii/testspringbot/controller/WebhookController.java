package com.ataraxii.testspringbot.controller;

import com.ataraxii.testspringbot.BookingBot;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final BookingBot bookingBot;

    @PostMapping("/webhook")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bookingBot.onWebhookUpdateReceived(update);
    }
}
