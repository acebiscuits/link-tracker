package backend.academy.linktracker.bot.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
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
import backend.academy.linktracker.bot.command.handler.impl.ListLinksCommandHandler;
import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.mapper.ScrapperClientExceptionMapper;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.LinkResponseDto;
import backend.academy.linktracker.bot.model.dto.ListLinksResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;

@ExtendWith(MockitoExtension.class)
public class ListLinksCommandHandlerTest {
    @Mock
    private ChatConversationDataRepository chatConversationDataRepository;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private ScrapperClientExceptionMapper scrapperClientExceptionMapper;

    @InjectMocks
    private ListLinksCommandHandler linksCommandHandler;

    private CommandData commandData;
    // private ConversationData conversationData;

    @BeforeEach
    void setup() {
        commandData = CommandData.builder()
                .command("/list")
                .description("Show all commands")
                .responses(Map.of(
                        "waiting-tags", "<waiting-tags>",
                        "not-found", "<chat-doesn't-exist>"))
                .build();
    }

    private UserMessageDto userMessage(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);

        return UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();
    }

    @Test
    @DisplayName("Should choose start listing step and return correct BotResponseDto")
    void test1() {
        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .build();

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
                .state(ConversationState.IDLE)
                .build());

        BotResponseDto response = null;
        response = linksCommandHandler.handle(userMessageDto, commandData);

        verify(chatConversationDataRepository, times(1)).get(userMessageDto.chatId());
        verify(chatConversationDataRepository, times(1)).set(userMessageDto.chatId(), ConversationData.builder()
                .state(ConversationState.WAITING_TAGS)
                .commandName(commandData.command())
                .build());

        assertThat(response).isNotNull();
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<waiting-tags>");
    }

    @Test
    @DisplayName("Should choose handleWaitingTags step and return link without tags")
    void test2() {
        UserMessageDto userMessageDto = userMessage("-");

        when(chatConversationDataRepository.get(userMessageDto.chatId())).thenReturn(ConversationData.builder()
                .state(ConversationState.WAITING_TAGS)
                .commandName("/list")
                .build());

        List<String> tags = List.of();
        when(scrapperClient.getLinks(userMessageDto.chatId(), tags))
                .thenReturn(
                        ListLinksResponseDto.builder()
                                .links(
                                        List.of(
                                                LinkResponseDto.builder()
                                                        .id(1L)
                                                        .url(URI.create("https://github.comm/test-link-2-no-tags"))
                                                        .build()))
                                .build());

        BotResponseDto response = null;
        response = linksCommandHandler.handle(userMessageDto, commandData);

        verify(chatConversationDataRepository, times(1)).get(userMessageDto.chatId());
        verify(scrapperClient, times(1)).getLinks(userMessageDto.chatId(), List.of());
        verify(chatConversationDataRepository, times(1)).remove(userMessageDto.chatId());

        assertThat(response).isNotNull();
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).contains("URL: https://github.comm/test-link-2-no-tags");
    }

    @Test
    @DisplayName("Should choose handleWaitingTags step and return exception message")
    void test3() {
        UserMessageDto userMessageDto = userMessage("-");

        ScrapperClientException exception = new ScrapperClientException(404, "Chat not found", new RuntimeException("test cause"));
        when(chatConversationDataRepository.get(userMessageDto.chatId())).thenReturn(ConversationData.builder()
                .state(ConversationState.WAITING_TAGS)
                .commandName("/list")
                .build());

        List<String> tags = List.of();
        when(scrapperClient.getLinks(userMessageDto.chatId(), tags))
                .thenThrow(exception);

        when(scrapperClientExceptionMapper.toBotResponseDto(userMessageDto.chatId(), commandData,
                exception))
                .thenReturn(new BotResponseDto(userMessageDto.chatId(), commandData.response("not-found")));

        BotResponseDto response = null;
        response = linksCommandHandler.handle(userMessageDto, commandData);

        verify(chatConversationDataRepository, times(1)).get(userMessageDto.chatId());
        verify(scrapperClient, times(1)).getLinks(userMessageDto.chatId(), List.of());
        verify(scrapperClientExceptionMapper, times(1)).toBotResponseDto(userMessageDto.chatId(), commandData, exception);

        assertThat(response).isNotNull();
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<chat-doesn't-exist>");
    }
}
