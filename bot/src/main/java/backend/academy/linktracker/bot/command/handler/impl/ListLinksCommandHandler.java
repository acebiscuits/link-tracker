package backend.academy.linktracker.bot.command.handler.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.annotation.CommandHandlerAnnotation;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
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
import lombok.RequiredArgsConstructor;

@Component
@CommandHandlerAnnotation(commandName = "list-links")
@RequiredArgsConstructor
public class ListLinksCommandHandler implements BaseCommandHandler {

    private final ChatConversationDataRepository chatConversationDataRepository;

    private final ScrapperClient scrapperClient;

    private final ScrapperClientExceptionMapper scrapperClientExceptionMapper;

    @Override
    public BotResponseDto handle(UserMessageDto messageDto, CommandData commandData) {

        ConversationData conversationData = chatConversationDataRepository.get(messageDto.chatId());

        return switch(conversationData.state()) {
            case IDLE -> startListing(messageDto, commandData);
            case WAITING_TAGS -> handleWaitingTags(messageDto, conversationData, commandData);
            default -> startListing(messageDto, commandData);
        };
    }

    private BotResponseDto startListing(UserMessageDto messageDto, CommandData commandData) {
        chatConversationDataRepository.set(
            messageDto.chatId(), 
            ConversationData.builder()
                .commandName(commandData.command())
                .state(ConversationState.WAITING_TAGS)
                .build()
        );

        return new BotResponseDto(
            messageDto.chatId(),
            commandData.response("waiting-tags")
        );
    }

    private BotResponseDto handleWaitingTags(UserMessageDto messageDto, ConversationData conversationData, CommandData commandData) {
        List<String> tags = parseList(messageDto.update().message().text());
        return finishListing(messageDto, commandData, tags);
    }

    private BotResponseDto finishListing(UserMessageDto messageDto, CommandData commandData, List<String> tags) {
        ListLinksResponseDto linksResponse;

        try {
            linksResponse = scrapperClient.getLinks(messageDto.chatId(), tags);
        }
        catch (ScrapperClientException ex) {
            return scrapperClientExceptionMapper.toBotResponseDto(messageDto.chatId(), commandData, ex);
        }

        if (linksResponse == null || linksResponse.links() == null || linksResponse.links().isEmpty()) {
            chatConversationDataRepository.remove(messageDto.chatId());
            return new BotResponseDto(messageDto.chatId(), commandData.response("empty-lsit"));
        }

        chatConversationDataRepository.remove(messageDto.chatId());

        return new BotResponseDto(messageDto.chatId(), formatLinks(linksResponse.links(), commandData));
    
    }

    private String formatLinks(List<LinkResponseDto> links, CommandData commandData) {
        StringBuilder sb = new StringBuilder();
        sb.append("Links:");
        sb.append("\n\n");
        sb.append(links.stream().map(this::formatLink).collect(Collectors.joining("\n\n")));
        return sb.toString();
    }

    private String formatLink(LinkResponseDto link) {
        return """
            ID: %s
            URL: %s
            Tags: %s
            Filters: %s
            """.formatted(
                link.id(),
                link.url(),
                formatList(link.tags()),
                formatList(link.filters())
            ).trim();
    }

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "-";
        }

        return String.join(", ", values);
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
    
}
