package com.ataraxii.testspringbot;

import com.ataraxii.testspringbot.handler.BookingHandler;
import com.ataraxii.testspringbot.handler.StepHandler;
import com.ataraxii.testspringbot.handler.callback.CallbackQueryHandler;
import com.ataraxii.testspringbot.keyboard.KeyboardFactory;
import com.ataraxii.testspringbot.model.BookingStep;
import com.ataraxii.testspringbot.properties.TelegramBotProperties;
import com.ataraxii.testspringbot.service.telegram.BookingStateService;
import com.ataraxii.testspringbot.service.telegram.TelegramBotExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingBot extends TelegramWebhookBot implements TelegramBotExecutor {

    private final TelegramBotProperties properties;
    private final BookingHandler bookingHandler;
    private final BookingStateService stateService;
    private final CallbackQueryHandler callbackQueryHandler;
    private final KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            // 1️⃣ Обработка CallbackQuery
            if (update.hasCallbackQuery()) {
                log.info("Получен CallbackQuery от {}: {}",
                        update.getCallbackQuery().getFrom().getFirstName(),
                        update.getCallbackQuery().getData());
                callbackQueryHandler.handleCallback(update);
                return null;
            }

            // 2️⃣ Проверка, что есть сообщение
            if (!update.hasMessage()) {
                log.warn("Получен update без сообщения: {}", update);
                return null;
            }

            Long chatId = update.getMessage().getChatId();
            Object input;

            // 3️⃣ Определяем тип входа
            if (update.getMessage().hasContact()) {
                log.info("Получен контакт. chatId = {}, phone = {}",
                        chatId, update.getMessage().getContact().getPhoneNumber());
                input = update; // контакты передаем как Update
            } else if (update.getMessage().hasText()) {
                String text = update.getMessage().getText().trim();
                input = text;

                // Специальная обработка /start
                if ("/start".equals(text)) {
                    stateService.setStep(chatId, BookingStep.VISIT_TYPE);
                }
            } else {
                return SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("⚠️ Я могу принимать только текст или контакт.")
                        .build();
            }

            // 4️⃣ Получаем текущий шаг и соответствующий обработчик
            BookingStep step = stateService.getStep(chatId);
            StepHandler<?> handler = bookingHandler.getHandler(step);

            if (handler == null) {
                return SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("❌ Нет обработчика для текущего шага")
                        .build();
            }

            // 5️⃣ Проверка типа данных через getGenericClass()
            if (!handler.getGenericClass().isInstance(input)) {
                return SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("❌ Некорректный тип данных для текущего шага")
                        .build();
            }

            // 6️⃣ Вызов StepHandler
            BotApiMethod<?> response;
            if (handler.getGenericClass() == String.class) {
                response = ((StepHandler<String>) handler).handle(chatId, (String) input);
            } else {
                response = ((StepHandler<Update>) handler).handle(chatId, (Update) input);
            }

            // 7️⃣ После успешного бронирования контакта возвращаем стартовую клавиатуру
            if (step == BookingStep.CONTACT_CONFIRM && response instanceof SendMessage sendMessage) {
                stateService.setStep(chatId, BookingStep.VISIT_TYPE);
                sendMessage.setReplyMarkup(keyboardFactory.getKeyboardForStep(BookingStep.VISIT_TYPE));
            }

            return response;

        } catch (Exception e) {
            log.error("Ошибка в BookingBot", e);
            Long chatId = update.hasMessage() ? update.getMessage().getChatId() : 0L;
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Произошла ошибка при обработке вашего сообщения.")
                    .build();
        }
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    @Override
    public String getBotPath() {
        return "/webhook";
    }

    @Override
    public void execute(SendMessage message) throws TelegramApiException {
        super.execute(message);
    }

    @Override    public void execute(EditMessageText message) throws TelegramApiException {
        super.execute(message);
    }
}
