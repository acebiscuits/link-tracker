package backend.academy.linktracker.bot.service.notification.receiver.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import backend.academy.linktracker.bot.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.bot.service.LinkUpdateNotificationService;
import backend.academy.linktracker.bot.service.notification.receiver.LinkUpdatesReceiver;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
@ConditionalOnProperty(prefix = "app.notification", name = "transport", havingValue = "HTTP")
public class HttpLinkUpdateReceiver implements LinkUpdatesReceiver {

    private final LinkUpdateNotificationService linkUpdateNotificationService;

    public void receive(LinkUpdateRequestDto update) {
        linkUpdateNotificationService.process(update);
    }

}
