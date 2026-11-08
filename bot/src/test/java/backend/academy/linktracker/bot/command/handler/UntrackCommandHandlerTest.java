package backend.academy.linktracker.bot.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.handler.impl.UntrackCommandHandler;
import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.mapper.ScrapperClientExceptionMapper;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.RemoveLinkRequestDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;

@ExtendWith(MockitoExtension.class)
public class UntrackCommandHandlerTest {
    
    @Mock
    ChatConversationDataRepository chatConversationDataRepository;

    @Mock
    ScrapperClient scrapperClient;

    @Mock
    ScrapperClientExceptionMapper scrapperClientExceptionMapper;

    @InjectMocks
    UntrackCommandHandler untrackCommandHandler;

    private CommandData commandData;

    @BeforeEach
    void setup() {
        commandData = CommandData.builder()
                .command("/untrack")
                .description("Add link to track")
                .responses(Map.of(
                    "waiting-link", "<waiting-link>",
                    "invalid-link", "<invalid-link>",
                    "success", "<link-deleted>",
                    "not-found", "<not-found>"
                ))
                .build();
    }

    @Test
    @DisplayName("Should start link input step and return response")
    void test1() {
        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .build();

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
            .state(ConversationState.IDLE)
            .build());

        BotResponseDto response = untrackCommandHandler.handle(userMessageDto, commandData);

        assertThat(response).isNotNull();
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<waiting-link>");
    }

    @Test
    @DisplayName("Should untrack link and return response")
    void test2() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
            .state(ConversationState.WAITING_LINK)
            .build());

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("https://github.comm/test-link");

        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();

        BotResponseDto response = untrackCommandHandler.handle(userMessageDto, commandData);

        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<link-deleted>");

        verify(scrapperClient).removeLink(1L, new RemoveLinkRequestDto(URI.create("https://github.comm/test-link")));
        verify(chatConversationDataRepository).remove(1L);
    }

    @Test
    @DisplayName("Should catch scrapper exception and convert it to scrapperClientException")
    void test3() {
        ScrapperClientException exception = new ScrapperClientException(404, "Chat not found", new RuntimeException("test cause"));

        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
            .state(ConversationState.WAITING_LINK)
            .build());

            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("https://github.comm/test-link");
            
            UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();
            
        when(scrapperClientExceptionMapper.toBotResponseDto(userMessageDto.chatId(), commandData, exception))
            .thenReturn(new BotResponseDto(userMessageDto.chatId(), commandData.response("not-found")));

        when(scrapperClient.removeLink(1L, new RemoveLinkRequestDto(URI.create("https://github.comm/test-link")))).thenThrow(exception);

        BotResponseDto response = untrackCommandHandler.handle(userMessageDto, commandData);
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<not-found>");
    }
}
