package pro.sky.telegrambot.service;


public interface MessageSender {
    void sendMessage(long chatId, String message);
}
