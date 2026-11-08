package backend.academy.linktracker.bot.command.handler.impl;

import java.net.URI;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.annotation.CommandHandlerAnnotation;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.mapper.ScrapperClientExceptionMapper;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.RemoveLinkRequestDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@CommandHandlerAnnotation(commandName = "untrack")
@RequiredArgsConstructor
@Slf4j
public class UntrackCommandHandler implements BaseCommandHandler {
    
    private final ChatConversationDataRepository chatConversationDataRepository;

    private final ScrapperClient scrapperClient;

    private final ScrapperClientExceptionMapper scrapperExceptionMapper;

    @Override
    public BotResponseDto handle(UserMessageDto messageDto, CommandData commandData) {
        ConversationData conversationData = chatConversationDataRepository.get(messageDto.chatId());

        return switch(conversationData.state()) {
            case IDLE -> startUntracking(messageDto, commandData);
            case WAITING_LINK -> handleWaitingLink(messageDto, conversationData, commandData);
            default -> restartUntracking(messageDto, commandData);
        };
    }

    private BotResponseDto startUntracking(UserMessageDto messageDto, CommandData commandData) {
        chatConversationDataRepository.set(
            messageDto.chatId(),
            ConversationData.builder()
                .commandName(commandData.command())
                .state(ConversationState.WAITING_LINK)
                .build()
        );

        return new BotResponseDto(
            messageDto.chatId(),
            commandData.response("waiting-link"));
    }

    private BotResponseDto handleWaitingLink(UserMessageDto messageDto, ConversationData conversationData, CommandData commandData) {
        URI url;

        try {
            url = URI.create(messageDto.update().message().text().trim());
        } catch (Exception ex) {
            return new BotResponseDto(messageDto.chatId(), commandData.response("invalid-link"));
        }

        try {
            scrapperClient.removeLink(
                messageDto.chatId(),
                new RemoveLinkRequestDto(url));
        }
        catch (ScrapperClientException ex) {
            return scrapperExceptionMapper.toBotResponseDto(messageDto.chatId(), commandData, ex);
        }

        chatConversationDataRepository.remove(messageDto.chatId());

        return new BotResponseDto(messageDto.chatId(), commandData.response("success"));
    }

    private BotResponseDto restartUntracking(UserMessageDto messageDto, CommandData commandData) {
        chatConversationDataRepository.remove(messageDto.chatId());
        return startUntracking(messageDto, commandData);
    }
}
