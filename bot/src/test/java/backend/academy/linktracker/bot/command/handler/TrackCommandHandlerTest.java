package backend.academy.linktracker.bot.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.handler.impl.TrackCommandHandler;
import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.mapper.ScrapperClientExceptionMapper;
import backend.academy.linktracker.bot.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;

@ExtendWith(MockitoExtension.class)
public class TrackCommandHandlerTest {
    
    @Mock
    ChatConversationDataRepository chatConversationDataRepository;

    @Mock
    ScrapperClient scrapperClient;

    @Mock
    private ScrapperClientExceptionMapper scrapperClientExceptionMapper;

    @InjectMocks
    TrackCommandHandler trackCommandHandler;

    private CommandData commandData;

    @BeforeEach
    void setup() {
        commandData = CommandData.builder()
                .command("/track")
                .description("Add link to track")
                .responses(Map.of(
                    "waiting-link", "<waiting-link>",
                    "waiting-tags", "<waiting-tags>",
                    "waiting-filters", "<waiting-filters>",
                    "success", "<link-added>",
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

        BotResponseDto response = trackCommandHandler.handle(userMessageDto, commandData);

        assertThat(response).isNotNull();
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<waiting-link>");
    }

    @Test
    @DisplayName("Should save link, request tags and return response")
    void test2() {

        MessageEntity textLinkEntity = mock(MessageEntity.class);
        when(textLinkEntity.url()).thenReturn("https://github.comm/test-link");
        when(textLinkEntity.type()).thenReturn(MessageEntity.Type.text_link);

        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.entities()).thenReturn(new MessageEntity[]{textLinkEntity});

        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
            .state(ConversationState.WAITING_LINK)
            .build());

        BotResponseDto response = null;
        response = trackCommandHandler.handle(userMessageDto, commandData);

        verify(chatConversationDataRepository).set(1L, ConversationData.builder()
            .url(URI.create("https://github.comm/test-link"))
            .state(ConversationState.WAITING_TAGS)
            .build());

        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<waiting-tags>");
        verify(chatConversationDataRepository).set(1L, ConversationData.builder()
            .url(URI.create("https://github.comm/test-link"))
            .state(ConversationState.WAITING_TAGS)
            .build());
    }

    @Test
    @DisplayName("Should save tags, request filters and return response")
    void test3() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("<tag-1>, <tag-2>");

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
            .state(ConversationState.WAITING_TAGS)
            .build());

        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();

        BotResponseDto response = trackCommandHandler.handle(userMessageDto, commandData);

        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<waiting-filters>");
        verify(chatConversationDataRepository).set(1L, ConversationData.builder()
            .tags(List.of("<tag-1>", "<tag-2>"))
            .state(ConversationState.WAITING_FILTERS)
            .build());
    }

    @Test
    @DisplayName("Should save filters, save link data in scrarpper, clean link info and return response")
    void test4() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("<filter-1>, <filter-2>");

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
            .state(ConversationState.WAITING_FILTERS)
            .url(URI.create("https://github.comm/test-link"))
            .tags(List.of("<tag-1>", "<tag-2>"))
            .build());

        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();

        BotResponseDto response = trackCommandHandler.handle(userMessageDto, commandData);

        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<link-added>");

        verify(scrapperClient).addLink(1L, AddLinkRequestDto.builder()
                    .link(URI.create("https://github.comm/test-link"))
                    .tags(List.of("<tag-1>", "<tag-2>"))
                    .filters(List.of("<filter-1>", "<filter-2>"))
                    .build());

        verify(chatConversationDataRepository).remove(1L);
    }

    @Test
    @DisplayName("Should catch scrapper exception and convert it to scrapperClientException")
    void test5() {
        ScrapperClientException exception = new ScrapperClientException(404, "Chat not found", new RuntimeException("test cause"));

        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("<filter-1>, <filter-2>");

        when(chatConversationDataRepository.get(1L)).thenReturn(ConversationData.builder()
            .state(ConversationState.WAITING_FILTERS)
            .url(URI.create("https://github.comm/test-link"))
            .tags(List.of("<tag-1>", "<tag-2>"))
            .build());
            
        UserMessageDto userMessageDto = UserMessageDto.builder()
            .chatId(1L)
            .update(update)
            .build();
         when(scrapperClientExceptionMapper.toBotResponseDto(userMessageDto.chatId(), commandData,
                exception))
                .thenReturn(new BotResponseDto(userMessageDto.chatId(), commandData.response("not-found")));


        when(scrapperClient.addLink(1L,  AddLinkRequestDto.builder()
                    .link(URI.create("https://github.comm/test-link"))
                    .tags(List.of("<tag-1>", "<tag-2>"))
                    .filters(List.of("<filter-1>", "<filter-2>"))
                    .build())).thenThrow(exception);

        BotResponseDto response = trackCommandHandler.handle(userMessageDto, commandData);
        assertThat(response.chatId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("<not-found>");
    }
}
