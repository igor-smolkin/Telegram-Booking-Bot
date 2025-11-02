package com.ataraxii.testspringbot.bot.service.google;

import com.ataraxii.testspringbot.bot.model.BookingData;
import com.ataraxii.testspringbot.bot.model.BookingStatus;
import com.ataraxii.testspringbot.bot.properties.GoogleProperties;
import com.ataraxii.testspringbot.db.service.BotSettingsService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    private final Sheets sheetsService;
    private final BotSettingsService settingsService;

    public void appendBooking(BookingData booking) {
        booking.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        List<Object> row = List.of(
                booking.getId(),
                booking.getVisitType(),
                booking.getDuration(),
                booking.getPeopleCount(),
                booking.getDate().toString(),
                booking.getTime() != null ? booking.getTime().toString() : "",
                booking.getPhone(),
                booking.getStatus().toString(),
                booking.getApprovedBy() != null ? booking.getApprovedBy() : ""
        );

        ValueRange body = new ValueRange().setValues(List.of(row));
        try {
            sheetsService.spreadsheets().values()
                    .append(settingsService.getGoogleSpreadsheetId(), "A1", body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Бронирование {} добавлено в Google Sheets", booking.getId());
        } catch (IOException e) {
            log.error("Ошибка при добавлении бронирования в таблицу", e);
        }
    }

    @Async
    public void updateStatusAsync(Long bookingId, BookingStatus status, String approvedBy) {
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(settingsService.getGoogleSpreadsheetId(), "A2:I")
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null) return;

            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.isEmpty()) continue;

                if (row.get(0).toString().equals(bookingId.toString())) {
                    List<Object> updatedRow = new ArrayList<>(row);
                    while (updatedRow.size() < 9) updatedRow.add("");

                    updatedRow.set(7, status.toString());
                    updatedRow.set(8, approvedBy != null ? approvedBy : "");

                    ValueRange body = new ValueRange().setValues(List.of(updatedRow));
                    String range = String.format("A%d:I%d", i + 2, i + 2);

                    int attempts = 0;
                    while (attempts < 3) {
                        try {
                            sheetsService.spreadsheets().values()
                                    .update(settingsService.getGoogleSpreadsheetId(), range, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            log.info("Booking {} обновлен в Google Sheets: {} / {}", bookingId, status, approvedBy);
                            break;
                        } catch (SocketTimeoutException e) {
                            attempts++;
                            log.warn("Таймаут при обновлении Google Sheets (попытка {}), bookingId={}", attempts, bookingId);
                            Thread.sleep(1000);
                        }
                    }
                    return;
                }
            }
            log.warn("BookingId {} не найден в Google Sheets", bookingId);

        } catch (Exception e) {
            log.error("Ошибка при обновлении статуса в Google Sheets, bookingId={}", bookingId, e);
        }
    }

    public BookingData findBookingById(Long id) {
        long start = System.currentTimeMillis();
        log.info("Поиск брони с id={} в Google Sheets...", id);

        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(settingsService.getGoogleSpreadsheetId(), "A2:I")
                    .execute();

            List<List<Object>> rows = response.getValues();

            if (rows == null || rows.isEmpty()) {
                log.warn("Данные в Google Sheets отсутствуют (пустой лист)");
                return null;
            }

            for (List<Object> row : rows) {
                if (row.get(0).toString().equals(id.toString())) {
                    BookingData data = new BookingData();
                    data.setId(id);
                    data.setVisitType(row.get(1).toString());
                    data.setDuration(row.get(2).toString());
                    data.setPeopleCount(Integer.parseInt(row.get(3).toString()));
                    data.setDate(LocalDate.parse(row.get(4).toString()));
                    data.setTime(LocalTime.parse(row.get(5).toString()));
                    data.setPhone(row.get(6).toString());
                    data.setStatus(BookingStatus.valueOf(row.get(7).toString()));
                    if (row.size() > 8)
                        data.setApprovedBy(row.get(8).toString());

                    log.info("Бронь найдена: {} (время выполнения {} мс)",
                            data, System.currentTimeMillis() - start);

                    return data;
                }
            }

            log.warn("Бронь с id={} не найдена (время выполнения {} мс)",
                    id, System.currentTimeMillis() - start);
            return null;

        } catch (Exception e) {
            log.error("Ошибка при поиске брони id={}: {}", id, e.getMessage(), e);
            return null;
        }
    }
}
