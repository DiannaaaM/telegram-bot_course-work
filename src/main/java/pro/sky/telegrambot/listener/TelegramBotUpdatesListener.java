package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                logger.info("Processing update: {}", update);

                if (update.message() == null || update.message().text() == null) {
                    continue;
                }

                String messageText = update.message().text();
                long chatId = update.message().chat().id();

                for (String msg : handleNewMessage(messageText, chatId)) {
                    telegramBot.execute(new SendMessage(chatId, msg));
                }
            } catch (Exception e) {
                logger.error("Error while processing update: ", e);
                telegramBot.execute(new SendMessage(
                        update.message().chat().id(),
                        "Произошла ошибка при обработке вашего сообщения."
                ));
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private List<String> handleNewMessage(String messageText, long chatId) {
        if ("/start".equals(messageText)) {
            logger.info("Sent /start response to chat ID: {}", chatId);
            return List.of(
                    "Привет!",
                    "Я бот для планирования задач."
            );
        }

        if ("Start note tasks".equals(messageText)) {
            return List.of("I'm ready to start note tasks");
        }

        if (messageText.matches("^\\d.*")) {
            notificationTaskService.processMessage(chatId, messageText);
            return List.of("Great!");
        }

        return List.of("Я получил сообщение, но пока не понимаю как его обработать...");
    }
}
