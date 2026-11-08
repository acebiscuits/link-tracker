package backend.academy.linktracker.bot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import backend.academy.linktracker.bot.command.CommandDispatcher;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateProcessorService {

    private final CommandDispatcher commandDispatcher;

    private final MessageSender senderService;
    
    public int processUpdates(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) continue;

            processUpdate(update);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        try {
            BotResponseDto botResponseDto = commandDispatcher.handleUpdate(new UserMessageDto(update.message().chat().id(), update));

            senderService.sendResponse(botResponseDto);
        } catch (Exception ex) {
            log.error("Failed to process update. updateId={}", update.updateId(), ex);
        }
    }
}
