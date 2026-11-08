package backend.academy.linktracker.bot.command.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pengrad.telegrambot.model.Update;

import backend.academy.linktracker.bot.command.handler.impl.UnknownCommandHandler;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;

public class UnknownCommandHandlerTest {
    
    private UnknownCommandHandler unknownCommandHandler;

    private UserMessageDto userMessageDto;
    private CommandData commandData;

    @BeforeEach
    public void setup() {
        unknownCommandHandler = new UnknownCommandHandler();

        userMessageDto = UserMessageDto.builder()
                            .chatId(1L)
                            .update(new Update())
                            .build();

        commandData = CommandData.builder()
                        .responses(Map.of("unknowing", "<unknown>"))
                        .build();
    }

    @Test
    @DisplayName("Should return response")
    void test1() {
        BotResponseDto response = unknownCommandHandler.handle(userMessageDto, commandData);

        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<unknown>");
    }
}
