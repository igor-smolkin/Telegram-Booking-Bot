package com.ataraxii.testspringbot.listener;

import com.ataraxii.testspringbot.events.BookingCreatedEvent;
import com.ataraxii.testspringbot.keyboard.KeyboardFactory;
import com.ataraxii.testspringbot.model.BookingData;
import com.ataraxii.testspringbot.service.google.GoogleSheetsService;
import com.ataraxii.testspringbot.service.telegram.TelegramSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private final TelegramSenderService senderService;
    private final KeyboardFactory keyboardFactory;
    private final GoogleSheetsService sheetsService;

    @Async
    @EventListener
    public void onBookingCreated(BookingCreatedEvent event) {
        BookingData booking = event.getBookingData();

        SendMessage message = SendMessage.builder()
                .chatId(event.getAdminChatId().toString())
                .text(formatAdminMessage(booking))
                .replyMarkup(keyboardFactory.adminBookingKeyboard(booking.getId()))
                .build();

        sheetsService.appendBooking(booking);
        senderService.sendMessage(message);
        log.info("Бронь {} отправлена в Google Sheets", booking.getId());
    }

    private String formatAdminMessage(BookingData booking) {
        String statusText;
        switch (booking.getStatus()) {
            case CONFIRMED -> statusText = "✅ ПОДТВЕРЖДЕНА";
            case REJECTED -> statusText = "❌ ОТМЕНЕНА";
            default -> statusText = "⏳ В ОЖИДАНИИ";
        }

        return String.format(
                "🆔 Запись №%d\n" +
                        "\n" +
                        "🏷️ Тип посещения: %s\n" +
                        "⏱️ Длительность: %s\n" +
                        "👥 Количество человек: %d\n" +
                        "📅 Дата: %s\n" +
                        "⏰ Время: %s\n" +
                        "📞 Контакт: %s\n" +
                        "\n" +
                        "🔖 Статус: %s",
                booking.getId(),
                booking.getVisitType(),
                booking.getDuration(),
                booking.getPeopleCount(),
                booking.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                booking.getTime(),
                booking.getPhone(),
                statusText
        );
    }
}
