package backend.academy.linktracker.scrapper.service.notification;

import backend.academy.linktracker.scrapper.model.dto.LinkUpdateRequestDto;

public interface NotificationSender {
    
    void send(LinkUpdateRequestDto update);
}
