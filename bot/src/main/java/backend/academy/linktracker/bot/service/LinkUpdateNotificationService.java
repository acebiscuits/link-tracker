package backend.academy.linktracker.bot.service;

import org.springframework.stereotype.Service;

import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.bot.service.formatter.LinkUpdateMessageFormatter;
import backend.academy.linktracker.bot.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkUpdateNotificationService {

    private final MessageSender senderService;
    private final LinkUpdateMessageFormatter formatter;
    
    public void process(LinkUpdateRequestDto request) {
        String message = formatter.format(request);

        for (Long chatId : request.tgChatIds()) {
            senderService.sendResponse(new BotResponseDto(chatId, message));
        }
    }
}
