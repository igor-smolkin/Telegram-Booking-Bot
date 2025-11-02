package com.ataraxii.testspringbot.service.telegram;

import org.springframework.stereotype.Service;
import com.ataraxii.testspringbot.model.BookingData;
import com.ataraxii.testspringbot.model.BookingStep;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingStateService {

    // Для пользователей (шаги и временные данные)
    private final Map<Long, BookingData> dataMap = new ConcurrentHashMap<>();
    private final Map<Long, Deque<BookingStep>> stepStack = new ConcurrentHashMap<>();

    // Для админов (доступ по bookingId)
    private final Map<Long, BookingData> bookingsById = new ConcurrentHashMap<>();

    // --- Методы для шагов пользователя ---
    public BookingStep getStep(Long chatId) {
        Deque<BookingStep> stack = stepStack.get(chatId);
        return (stack == null || stack.isEmpty()) ? BookingStep.VISIT_TYPE : stack.peek();
    }

    public void setStep(Long chatId, BookingStep step) {
        stepStack.computeIfAbsent(chatId, k -> new ArrayDeque<>()).push(step);
    }

    public BookingStep goBack(Long chatId) {
        Deque<BookingStep> stack = stepStack.get(chatId);
        if (stack == null || stack.size() <= 1) {
            return BookingStep.VISIT_TYPE;
        }
        stack.pop();
        return stack.peek();
    }

    public BookingData getData(Long chatId) {
        return dataMap.computeIfAbsent(chatId, k -> new BookingData());
    }

    public void clear(Long chatId) {
        dataMap.remove(chatId);
        stepStack.remove(chatId);
    }
    // --- Методы для админов ---
    public void saveBooking(BookingData booking) {
        bookingsById.put(booking.getId(), booking);
    }

    public BookingData getBooking(Long bookingId) {
        return bookingsById.get(bookingId);
    }

    public void clearBooking(Long bookingId) {
        bookingsById.remove(bookingId);
    }
}
