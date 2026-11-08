package backend.academy.linktracker.bot.command.handler.impl;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.annotation.CommandHandlerAnnotation;
import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@CommandHandlerAnnotation(commandName = "unknown")
public class UnknownCommandHandler implements BaseCommandHandler {
    
    @Override
    public BotResponseDto handle(UserMessageDto messageDto, CommandData commandData) {
        return new BotResponseDto(messageDto.chatId(), commandData.response("unknowing"));
    }
}
