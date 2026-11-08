package backend.academy.linktracker.scrapper.service.notification.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.scrapper.service.notification.NotificationSender;
import lombok.RequiredArgsConstructor;

@Service 
@ConditionalOnProperty(prefix = "app.notification", name = "transport", havingValue = "HTTP")
@RequiredArgsConstructor 
public class HttpNotificationSender implements NotificationSender {
    
    private final BotClient botClient;

    @Override 
    public void send(LinkUpdateRequestDto update) {
        botClient.sendUpdate(update);
    }
}
