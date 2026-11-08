package backend.academy.linktracker.bot.service.sender;

import backend.academy.linktracker.bot.model.dto.BotResponseDto;

public interface MessageSender {
    public void sendResponse(BotResponseDto responseDto);
}
