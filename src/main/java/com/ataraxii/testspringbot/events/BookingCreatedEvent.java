package com.ataraxii.testspringbot.events;

import com.ataraxii.testspringbot.model.BookingData;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingCreatedEvent extends ApplicationEvent {

    private final BookingData bookingData;
    private final Long adminChatId;

    public BookingCreatedEvent(Object source, BookingData bookingData, Long adminChatId) {
        super(source);
        this.bookingData = bookingData;
        this.adminChatId = adminChatId;
    }
}
