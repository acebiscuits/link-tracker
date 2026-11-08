package backend.academy.linktracker.bot.command.handler.impl;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.annotation.CommandHandlerAnnotation;
import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@CommandHandlerAnnotation(commandName = "start")
@RequiredArgsConstructor
public class StartCommandHandler implements BaseCommandHandler {

    private final ChatConversationDataRepository chatConversationDataRegistry;
    
    @Override
    public BotResponseDto handle(UserMessageDto messageDto, CommandData commandData) {
        chatConversationDataRegistry.set(
            messageDto.chatId(),
            ConversationData.builder()
                .state(ConversationState.IDLE)
                .build()
        );
        return new BotResponseDto(messageDto.chatId(), commandData.response("starting-conversation"));
    }
}
