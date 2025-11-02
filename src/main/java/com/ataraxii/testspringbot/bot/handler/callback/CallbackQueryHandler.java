package com.ataraxii.testspringbot.bot.handler.callback;

import com.ataraxii.testspringbot.bot.model.BookingData;
import com.ataraxii.testspringbot.bot.model.BookingStatus;
import com.ataraxii.testspringbot.bot.service.google.GoogleCalendarService;
import com.ataraxii.testspringbot.bot.service.google.GoogleSheetsService;
import com.ataraxii.testspringbot.bot.service.telegram.BookingStateService;
import com.ataraxii.testspringbot.bot.service.telegram.TelegramSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler {

    private final BookingStateService stateService;
    private final TelegramSenderService senderService;
    private final GoogleSheetsService sheetsService;
    private final GoogleCalendarService calendarService;

    public void handleCallback(Update update) {
        try {
            if (!update.hasCallbackQuery()) return;

            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            String adminName = callbackQuery.getFrom().getUserName();
            Long adminChatId = callbackQuery.getMessage().getChatId();
            Integer messageId = callbackQuery.getMessage().getMessageId();

            if (!(callbackData.startsWith("CONFIRM_") || callbackData.startsWith("REJECT_"))) {
                log.warn("Неизвестный callbackData: {}", callbackData);
                return;
            }

            Long bookingId;
            try {
                bookingId = Long.parseLong(callbackData.split("_")[1]);
            } catch (NumberFormatException e) {
                log.error("Невозможно извлечь bookingId из callbackData: {}", callbackData, e);
                return;
            }

            BookingData booking = sheetsService.findBookingById(bookingId);

            if (booking == null) {
                log.warn("BookingData с id {} не найден", bookingId);
                return;
            }

            // Меняем статус и записываем кто подтвердил/отклонил
            if (callbackData.startsWith("CONFIRM_")) {
                booking.setStatus(BookingStatus.CONFIRMED);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
            }
            booking.setApprovedBy(adminName);

            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                try {
                    log.info("Создаём событие в Google Calendar для брони № {}", booking.getId());
                    calendarService.addBookingEvent(booking);
                    log.info("Событие для брони № {} успешно добавлено в Google Calendar", booking.getId());
                } catch (Exception e) {
                    log.error("Ошибка при создании события в Google Calendar для брони № {}", booking.getId(), e);
                }
            }

            // Редактируем сообщение в админском чате (убираем кнопки)
            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(adminChatId.toString())
                    .messageId(messageId)
                    .text(formatAdminMessage(booking, callbackQuery))
                    .parseMode("HTML")
                    .build();
            senderService.editMessage(editMessage);

            // Обновляем Google Sheets
            try {
                sheetsService.updateStatusAsync(booking.getId(), booking.getStatus(), booking.getApprovedBy());
                log.info("Booking {} обновлен в Google Sheets с статусом {} и одобрен: {}",
                        booking.getId(), booking.getStatus(), booking.getApprovedBy());
            } catch (Exception e) {
                log.error("Ошибка при обновлении бронирования {} в Google Sheets", booking.getId(), e);
            }

            stateService.clearBooking(bookingId);

        } catch (Exception e) {
            log.error("Ошибка обработки CallbackQuery: {}", update, e);
        }
    }

    private String formatAdminMessage(BookingData data, CallbackQuery callbackQuery) {
        String statusText;
        switch (data.getStatus()) {
            case CONFIRMED -> statusText = "✅ ПОДТВЕРЖДЕНА";
            case REJECTED -> statusText = "❌ ОТМЕНЕНА";
            default -> statusText = "⏳ В ОЖИДАНИИ";
        }

        String approvedBy = "—";
        if (data.getApprovedBy() != null) {
            String username = callbackQuery.getFrom().getUserName();
            if (username != null) {
                approvedBy = String.format("<a href=\"https://t.me/%s\">@%s</a>", username, username);
            } else {
                approvedBy = data.getApprovedBy();
            }
        }

        // Формируем сообщение
        return String.format(
                "🆔 Запись № %d\n" +
                        "\n" +
                        "🏷️ Тип посещения: %s\n" +
                        "⏱️ Длительность: %s\n" +
                        "👥 Количество человек: %d\n" +
                        "📅 Дата: %s\n" +
                        "⏰ Время: %s\n" +
                        "📞 Контакт: %s\n" +
                        "\n" +
                        "🔖 Статус: %s\n" +
                        (data.getStatus() != BookingStatus.PENDING
                                ? "👤 Подтвердил запись: " + approvedBy
                                : ""),
                data.getId(),
                data.getVisitType(),
                data.getDuration(),
                data.getPeopleCount(),
                data.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                data.getTime(),
                data.getPhone(),
                statusText
        );
    }
}