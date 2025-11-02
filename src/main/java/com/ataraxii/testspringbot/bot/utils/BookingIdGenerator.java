package com.ataraxii.testspringbot.bot.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class BookingIdGenerator {

    private final Random random = new Random();

    public Long nextId() {
        long millis = System.currentTimeMillis() % 10000; // последние 4 цифры времени
        int suffix = random.nextInt(900) + 100; // 3 случайные цифры
        return Long.parseLong(String.format("%04d%03d", millis, suffix));
    }
}

