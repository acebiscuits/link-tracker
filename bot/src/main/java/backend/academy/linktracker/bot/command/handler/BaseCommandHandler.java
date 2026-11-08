package backend.academy.linktracker.bot.command.handler;

import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;

public interface BaseCommandHandler {

    public BotResponseDto handle(UserMessageDto messageDto, CommandData commandData);
}