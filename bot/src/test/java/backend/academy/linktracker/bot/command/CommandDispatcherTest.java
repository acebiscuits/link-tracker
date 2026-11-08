package backend.academy.linktracker.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
import backend.academy.linktracker.bot.command.registry.CommandHandlerRegistry;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.model.entity.RegisteredCommand;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;

@ExtendWith(MockitoExtension.class)
public class CommandDispatcherTest {
    
    @Mock
    private CommandHandlerRegistry commandHandlerRegistry;

    @Mock
    private ChatConversationDataRepository chatConversationDataRepository;

    @InjectMocks
    private CommandDispatcher commandDispatcher;

    @Test
    @DisplayName("Should, when command message, register new chat and invooke right handler handling")
    void test1() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/command");

        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();

        when(commandHandlerRegistry.findHandlerByCommand("/command"))
            .thenReturn(Optional.of(new RegisteredCommand("command", null, new TestCommandHandler())));
            
        BotResponseDto response = commandDispatcher.handleUpdate(userMessageDto);

        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<response>");

        verify(chatConversationDataRepository, times(2)).set(1L, ConversationData
                    .builder()
                    .state(ConversationState.IDLE)
                    .build());
    }


    private static class TestCommandHandler implements BaseCommandHandler {
    
        @Override
        public BotResponseDto handle(UserMessageDto userMessageDto, CommandData commandData) {
            return new BotResponseDto(1L, "<response>");
        }
    }

    @Test
    @DisplayName("Should continue handling pipeline when it's next message and return response")
    void test2() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("not command");

        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder().state(ConversationState.WAITING_LINK).commandName("/command").build());

        when(commandHandlerRegistry.findHandlerByCommand("/command"))
            .thenReturn(Optional.of(new RegisteredCommand("command", null, new TestCommandHandler())));

        BotResponseDto response = commandDispatcher.handleUpdate(userMessageDto);
        
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<response>");
    }

    @Test
    @DisplayName("Should register chat and invoke unknown command handler")
    void test3() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("not command");

        CommandData commandData = CommandData.builder()
            .command("/command")
            .description("command description")
            .build();

        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();

        BaseCommandHandler handler = new TestCommandHandler();


        when(chatConversationDataRepository.checkIfChatIsRegistered(1L)).thenReturn(false);

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder().state(ConversationState.IDLE).build());

        when(commandHandlerRegistry.unknownCommand()).thenReturn(new RegisteredCommand("command", commandData, handler));

        BotResponseDto response = commandDispatcher.handleUpdate(userMessageDto);
        
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<response>");
    }
}
