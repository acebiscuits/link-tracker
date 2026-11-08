package backend.academy.linktracker.bot.command.handler.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.model.MessageEntity;

import backend.academy.linktracker.bot.annotation.CommandHandlerAnnotation;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.mapper.ScrapperClientExceptionMapper;
import backend.academy.linktracker.bot.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@CommandHandlerAnnotation(commandName = "track")
@Slf4j
public class TrackCommandHandler implements BaseCommandHandler {

    private final ChatConversationDataRepository chatConversationDataRepository;

    private final ScrapperClient scrapperClient;

    private final ScrapperClientExceptionMapper scrapperClientExceptionMapper;

    @Override 
    public BotResponseDto handle(UserMessageDto messageDto, CommandData commandData) {

        ConversationData conversationData = chatConversationDataRepository.get(messageDto.chatId());
        
        return switch(conversationData.state()) {
            case IDLE -> startTracking(messageDto, commandData);
            case WAITING_LINK -> handleWaitingLink(messageDto, conversationData, commandData);
            case WAITING_TAGS -> handleWaitingTags(messageDto, conversationData, commandData);
            case WAITING_FILTERS -> handleWaitingFilters(messageDto, conversationData, commandData);
            default -> startTracking(messageDto, commandData);
        };
    }

    private BotResponseDto startTracking(UserMessageDto messageDto, CommandData commandData) {
        chatConversationDataRepository.set(
            messageDto.chatId(),
            ConversationData.builder()
                .commandName(commandData.command())
                .state(ConversationState.WAITING_LINK)
                .build()
        );

        return new BotResponseDto(
            messageDto.chatId(),
            commandData.response("waiting-link")
        );
    }

    private BotResponseDto handleWaitingLink(UserMessageDto messageDto, ConversationData conversationData, CommandData commandData) {
        URI url;

        try {
            url = parseUrl(extractLink(messageDto));
        } catch (Exception ex) {
            return new BotResponseDto(messageDto.chatId(), commandData.response("invalid-link"));
        }

        chatConversationDataRepository.set(
            messageDto.chatId(),
            conversationData.toBuilder()
                .url(url)
                .state(ConversationState.WAITING_TAGS)
                .build()
        );

        return new BotResponseDto(messageDto.chatId(), commandData.response("waiting-tags"));
    }

    private BotResponseDto handleWaitingTags(UserMessageDto messageDto, ConversationData conversationData, CommandData commandData) {
        List<String> tags = parseList(messageDto.update().message().text());

        chatConversationDataRepository.set(
            messageDto.chatId(),
            conversationData.toBuilder()
                .tags(tags)
                .state(ConversationState.WAITING_FILTERS)
                .build()
        );

        return new BotResponseDto(messageDto.chatId(), commandData.response("waiting-filters"));
    }

    private BotResponseDto handleWaitingFilters(UserMessageDto messageDto, ConversationData conversationData, CommandData commandData) {
        List<String> filters = parseList(messageDto.update().message().text());
        return finishTracking(messageDto, conversationData, commandData, filters);
    }

    private BotResponseDto finishTracking(UserMessageDto messageDto, ConversationData conversationData, CommandData commandData, List<String> filters) {
        try {
            scrapperClient.addLink(
                messageDto.chatId(),
                AddLinkRequestDto.builder()
                    .link(conversationData.url())
                    .tags(conversationData.tags())
                    .filters(filters)
                    .build()
                );
        }
        catch (ScrapperClientException ex) {
            return scrapperClientExceptionMapper.toBotResponseDto(messageDto.chatId(), commandData, ex);
        }

        chatConversationDataRepository.remove(messageDto.chatId());

        return new BotResponseDto(messageDto.chatId(), commandData.response("success"));
    }

    private List<String> parseList(String text) {
        if (text == null || text.isBlank() || "-".equals(text.trim())) {
            return List.of();
        }

        return Arrays.stream(text.split(","))
            .map(String::trim)
            .filter(tag -> !tag.isBlank())
            .toList();
    }

    private URI parseUrl(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("URL is empty");
        }

        URI url = URI.create(text.trim());

        if (url.getScheme() == null || url.getHost() == null) {
            throw new IllegalArgumentException("URL must contain scheme and host");
        }

        return url;
    }

    private String extractLink(UserMessageDto messageDto) {
        MessageEntity[] entities = messageDto.update().message().entities();
        for (MessageEntity entity: entities) {
            if(entity.type() == MessageEntity.Type.text_link && entity.url() != null && !entity.url().isBlank()) {
                return entity.url();
            }

            if (entity.type() == MessageEntity.Type.text_link && messageDto.update().message().text() != null) {
                int start = entity.offset();
                int end = start + entity.length();

                if (start >= 0 && end > start) {
                    return entity.url();
                }
                
            }
        }
        return messageDto.update().message().text();
    }
}