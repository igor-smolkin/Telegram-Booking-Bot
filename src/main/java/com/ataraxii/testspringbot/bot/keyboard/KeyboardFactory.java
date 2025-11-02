package com.ataraxii.testspringbot.bot.keyboard;

import com.ataraxii.testspringbot.bot.model.BookingStep;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class KeyboardFactory {

    // Кнопки для выбора типа посещения
    public ReplyKeyboardMarkup visitTypeKeyboard() {
        return keyboard(List.of("Свободное посещение", "Индивидуальная тренировка"));
    }

    // Кнопки для выбора длительности
    public ReplyKeyboardMarkup durationKeyboard() {
        return keyboard(List.of("30 минут", "1 час", "2 часа", "3 часа", "Безлимит", "Назад к выбору"));
    }

    // Кнопка "Назад к выбору"
    public ReplyKeyboardMarkup backKeyboard() {
        return singleRowKeyboard("Назад к выбору");
    }

    // Общая логика для клавиатур
    private ReplyKeyboardMarkup keyboard(List<String> buttons) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (String btn : buttons) {
            row.add(btn);
            if (row.size() == 2) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        if (!row.isEmpty()) rows.add(row);
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows);
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    public ReplyKeyboardMarkup singleRowKeyboard(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        row.addAll(Arrays.asList(buttons));
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    private ReplyKeyboardMarkup contactKeyboard() {
        KeyboardRow row = new KeyboardRow();
        KeyboardButton contactButton = new KeyboardButton("Поделиться контактом");
        contactButton.setRequestContact(true);
        row.add(contactButton);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    // Inline клавиатура для админов
    public InlineKeyboardMarkup adminBookingKeyboard(Long bookingId) {
        InlineKeyboardButton confirmBtn = InlineKeyboardButton.builder()
                .text("✅ Подтвердить")
                .callbackData("CONFIRM_" + bookingId)
                .build();

        InlineKeyboardButton rejectBtn = InlineKeyboardButton.builder()
                .text("❌ Отклонить")
                .callbackData("REJECT_" + bookingId)
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(confirmBtn, rejectBtn))
                .build();
    }

    // Промпты для каждого шага
    public String getPromptForStep(BookingStep step) {
        return switch (step) {
            case VISIT_TYPE -> "\uD83C\uDFAF Добро пожаловать! Выберите тип посещения";
            case DURATION -> "⏱\uFE0F Выберите длительность планируемого посещения";
            case PEOPLE_COUNT -> "\uD83D\uDC65 Укажите количество человек, планирующих посещение (например: 2)";
            case DATE -> "\uD83D\uDCC5 Введите дату посещения в формате ДД.ММ.ГГГГ (например: 29.10.2025)";
            case TIME -> "⏰ Введите время посещения в формате ЧЧ:ММ (например: 14:30)";
            case CONTACT_CONFIRM -> "\uD83D\uDCDE Пожалуйста, отправьте свой контакт, чтобы администратор мог связаться с вами и подтвердить бронирование.";
        };
    }

    // Отрисовка клавиатур во время каждого шага
    public ReplyKeyboardMarkup getKeyboardForStep(BookingStep step) {
        return switch (step) {
            case VISIT_TYPE -> visitTypeKeyboard();
            case DURATION -> durationKeyboard();
            case PEOPLE_COUNT, DATE, TIME -> backKeyboard();
            case CONTACT_CONFIRM -> contactKeyboard();
        };
    }
}
