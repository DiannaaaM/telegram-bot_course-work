package pro.sky.telegrambot.service;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

@Service
public class NotificationTaskService {
    private final MessageSender messageSender;
    private final NotificationTaskRepository notificationTaskRepository;
    private final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

    // Регулярное выражение для выделения даты и текста
    private static final String PATTERN = "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)";

    public NotificationTaskService(MessageSender messageSender, NotificationTaskRepository notificationTaskRepository) {
        this.messageSender = messageSender;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    // Метод для обработки строки
    public void processMessage(long chatId, String message) {
        // Шаг 1: Проверим, соответствует ли строка паттерну
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(message);

        if (!matcher.matches()) {
            logger.info("Невалидный формат сообщения");
            return;
        }

        // Шаг 2: Извлекаем дату и текст напоминания
        String dateTimeString = matcher.group(1);
        String reminderText = matcher.group(3);

        // Шаг 3: Преобразуем строку с датой и временем в LocalDateTime
        LocalDateTime sendTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        // Шаг 4: Создаём объект задачи для сохранения в БД
        NotificationTask task = new NotificationTask();
        task.setSendTime(sendTime);
        task.setMessage(reminderText);
        task.setChatId(chatId);

        // Шаг 5: Сохраняем задачу в базу данных (например, через репозиторий)
//            notificationTaskRepository.save(task);
        saveTaskToDatabase(task);
    }

    // Метод для сохранения задачи в БД (пример)
    public void saveTaskToDatabase(NotificationTask task) {
        // Здесь можно использовать ваш репозиторий для сохранения задачи в базу данных
        // Например, repository.save(task);
        notificationTaskRepository.saveAndFlush(task);
        logger.info("Задача сохранена: " + task);
    }

    @Scheduled(cron = "0/30 * * * * *")
    public void findNearbyTask() {
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = notificationTaskRepository.findBySendTimeIsLessThanEqual(currentTime);

        for (NotificationTask task : tasks) {
            messageSender.sendMessage( task.getChatId(), task.getMessage() );
            notificationTaskRepository.delete(task);
        }
    }
}



