package com.ataraxii.testspringbot.handler.main;

import com.ataraxii.testspringbot.events.BookingCreatedEvent;
import com.ataraxii.testspringbot.handler.StepHandler;
import com.ataraxii.testspringbot.keyboard.KeyboardFactory;
import com.ataraxii.testspringbot.model.BookingData;
import com.ataraxii.testspringbot.model.BookingStep;
import com.ataraxii.testspringbot.properties.TelegramBotProperties;
import com.ataraxii.testspringbot.service.telegram.BookingStateService;
import com.ataraxii.testspringbot.utils.BookingIdGenerator;
import com.ataraxii.testspringbot.utils.UpdateExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactHandler implements StepHandler<Update> {

    private final TelegramBotProperties properties;
    private final BookingStateService stateService;
    private final KeyboardFactory keyboardFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingIdGenerator bookingIdGenerator;

    @Override
    public BookingStep getStep() {
        return BookingStep.CONTACT_CONFIRM;
    }

    @Override
    public SendMessage handle(Long chatId, Update update) {
        Contact contact = UpdateExtractor.getContact(update);

        if (contact == null) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Пожалуйста, нажмите кнопку ниже, чтобы поделиться своим контактом 📱")
                    .replyMarkup(keyboardFactory.visitTypeKeyboard())
                    .build();
        }

        BookingData data = stateService.getData(chatId);

        data.setPhone(contact.getPhoneNumber());
        data.setUserChatId(chatId);

        if (data.getId() == null) {
            data.setId(bookingIdGenerator.nextId());
        }

        stateService.saveBooking(data);
        stateService.clear(chatId);

        String summary = String.format(
                "\uD83D\uDCCB Ваша заявка на бронирование принята!\n" +
                        "\n" +
                        "\uD83C\uDFF7\uFE0F Тип посещения: %s\n" +
                        "⏱\uFE0F Длительность: %s\n" +
                        "\uD83D\uDC65 Количество человек: %d\n" +
                        "\uD83D\uDCC5 Дата: %s\n" +
                        "⏰ Время: %s\n" +
                        "\uD83D\uDCDE Контакт: %s",
                data.getVisitType(),
                data.getDuration(),
                data.getPeopleCount(),
                data.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                data.getTime(),
                data.getPhone()
        );

        log.info("Публикуем BookingCreatedEvent для админ-группы {}", properties.getAdminGroupChatId());
        eventPublisher.publishEvent(new BookingCreatedEvent(this, data, properties.getAdminGroupChatId()));

        return SendMessage.builder()
                .chatId(chatId)
                .text(summary + "\n\n✅ Ожидайте звонок от администратора для подтверждения записи.")
                .replyMarkup(keyboardFactory.visitTypeKeyboard())
                .build();
    }

    @Override
    public Class<Update> getGenericClass() {
        return Update.class;
    }
}
