package backend.academy.linktracker.bot.service.notification.receiver;

import backend.academy.linktracker.bot.model.dto.LinkUpdateRequestDto;

public interface LinkUpdatesReceiver {
    public void receive(LinkUpdateRequestDto update);
}
