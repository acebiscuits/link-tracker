package backend.academy.linktracker.bot.model.entity;

import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;

public record RegisteredCommand(String commandName, CommandData commandData, BaseCommandHandler handler) {
}
