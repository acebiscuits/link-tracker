package backend.academy.linktracker.bot.service.sender;

import org.springframework.stereotype.Service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramSenderService implements MessageSender {
    
    private final TelegramBot bot;

    @Override
    public void sendResponse(BotResponseDto responseDto) {
        SendResponse response = bot.execute(new SendMessage(responseDto.chatId(), responseDto.message()));

        if (!response.isOk()) {
            log.warn("Failed to send to Telegram: chatId={}, errorCode={}, description={}",
                responseDto.chatId(),
                response.errorCode(),
                response.description()
            );
        }
    }
}
