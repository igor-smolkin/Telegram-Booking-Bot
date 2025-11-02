package com.ataraxii.testspringbot.service.google;

import com.ataraxii.testspringbot.model.BookingData;
import com.ataraxii.testspringbot.properties.GoogleProperties;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final Calendar calendarService;
    private final GoogleProperties properties;

    public void addBookingEvent(BookingData booking) {
        try {
            Event event = new Event()
                    .setSummary(abbreviateVisitType(booking.getVisitType()) + " - " + booking.getPeopleCount() + " чел.")
                    .setDescription(
                            "Запись № " + booking.getId() +
                            "\nКонтакт: " + booking.getPhone() +
                            "\nДлительность посещения: " + booking.getDuration());

            event.setColorId(getColorIdForDuration(booking.getDuration()));

            // Дата и время начала
            LocalDateTime startDateTime = LocalDateTime.of(booking.getDate(), booking.getTime());
            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                    .setTimeZone(ZoneId.systemDefault().toString());
            event.setStart(start);

            // Вычисляем время окончания
            LocalDateTime endDateTime = calculateEndDateTime(booking.getDuration(), startDateTime);
            EventDateTime end = new EventDateTime()
                    .setDateTime(new DateTime(endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                    .setTimeZone(ZoneId.systemDefault().toString());
            event.setEnd(end);

            calendarService.events().insert(properties.getCalendarId(), event).execute();
            log.info("Событие для бронирования {} добавлено в Google Calendar", booking.getId());
        } catch (IOException e) {
            log.error("Ошибка при добавлении события в Google Calendar для бронирования {}", booking.getId(), e);
        }
    }

    private LocalDateTime calculateEndDateTime(String duration, LocalDateTime startDateTime) {
        if (duration.contains("час")) {
            int hours = Integer.parseInt(duration.replaceAll("\\D+", ""));
            return startDateTime.plusHours(hours);
        } else if (duration.contains("мин")) {
            int minutes = Integer.parseInt(duration.replaceAll("\\D+", ""));
            return startDateTime.plusMinutes(minutes);
        } else if (duration.equalsIgnoreCase("безлимит")) {
            // Для безлимита заканчиваем в 22:00 того же дня
            LocalDateTime endOfDay = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.of(22, 0));
            return endOfDay.isAfter(startDateTime) ? endOfDay : startDateTime.plusHours(1); // на случай позднего начала
        }
        // дефолтное значение
        return startDateTime.plusMinutes(30);
    }

    /**
     * Подбирает цвет события в зависимости от длительности бронирования.
     * Google Calendar поддерживает 11 стандартных цветов (1–11)
     */
    private String getColorIdForDuration(String duration) {
        duration = duration.toLowerCase();

        if (duration.contains("30")) {
            return "10"; // Базилик
        } else if (duration.contains("1")) {
            return "7"; // Павлин
        } else if (duration.contains("2")) {
            return "9"; // Черника
        } else if (duration.contains("3")) {
            return "3"; // Виноград
        } else if (duration.contains("безлимит")) {
            return "11"; //
        }
        return "1"; // Дефолтный
    }

    private String abbreviateVisitType(String visitType) {
        return switch (visitType) {
            case "Свободное посещение" -> "Св. прыжки";
            case "Индивидуальная тренировка" -> "Индив.";
            default -> visitType; // если нет сокращения, оставляем полное
        };
    }
}
