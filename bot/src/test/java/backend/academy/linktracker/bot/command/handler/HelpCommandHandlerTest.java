package backend.academy.linktracker.bot.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pengrad.telegrambot.model.Update;

import backend.academy.linktracker.bot.command.handler.impl.HelpCommandHandler;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;

@ExtendWith(MockitoExtension.class)
public class HelpCommandHandlerTest {

    @Mock
    private ChatConversationDataRepository chatConversationDataRepository;

    @InjectMocks
    private HelpCommandHandler helpCommandHandler;

    private UserMessageDto userMessageDto;
    private CommandData commandData;

    @BeforeEach
    public void setup() {
        userMessageDto = UserMessageDto.builder()
                            .chatId(1L)
                            .update(new Update())
                            .build();

        commandData = CommandData.builder()
                        .command("/help")
                        .description("Show all commands")
                        .responses(Map.of("helping", "test"))
                        .build();
    }

    @Test
    @DisplayName("Help command handler hanlde func should return BotResponseDto and invoke chatConversationDataRegistry.set method")
    void test1() {
        BotResponseDto response = null;
        response = helpCommandHandler.handle(userMessageDto, commandData);

        verify(chatConversationDataRepository, times(1)).set(userMessageDto.chatId(), ConversationData.builder()
                .state(ConversationState.IDLE)
                .build());

        assertThat(response).isNotNull();
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("test");
    }
}