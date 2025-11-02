package com.ataraxii.testspringbot.bot.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingData {
    private Long id;
    private Long userChatId;
    private String visitType;
    private String duration;
    private int peopleCount;
    private LocalDate date;
    private LocalTime time;
    private String phone;
    private BookingStatus status = BookingStatus.PENDING;
    private String approvedBy;
}
