package backend.academy.linktracker.bot.service.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;

import backend.academy.linktracker.bot.service.UpdateProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Profile("!test")
@RequiredArgsConstructor
public class TelegramBotListenerService {

    private final TelegramBot bot;
    private final UpdateProcessorService updateProcessorService;

    @EventListener(ApplicationReadyEvent.class)
    private void registerUpdateListener() {
        bot.setUpdatesListener(updates -> {
            if (updates == null) {
                log.warn("Telegram updates list is null");
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
            return updateProcessorService.processUpdates(updates);
        });
    }
}
