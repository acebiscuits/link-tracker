package backend.academy.linktracker.bot.command.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.annotation.CommandHandlerAnnotation;
import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
import backend.academy.linktracker.bot.config.CommandProperties;
import backend.academy.linktracker.bot.exc.CommandMappingException;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.RegisteredCommand;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommandHandlerRegistry {//команда - хэндлер

    private final Map<String, RegisteredCommand> handlersByCommand;

    private final CommandProperties commandProperties;

    private static final String UNKNOWN_KEY = "unknown";

    public CommandHandlerRegistry(List<BaseCommandHandler> handlers, CommandProperties properties) {
        this.handlersByCommand = new HashMap<>();
        this.commandProperties = properties;

        for (BaseCommandHandler handler : handlers) {
            CommandHandlerAnnotation mapping = handler.getClass().getAnnotation(CommandHandlerAnnotation.class);
            if (mapping == null) {
                throw new CommandMappingException("Missing CommandMapping annotation for handler=" + handler.getClass().getName());
            }

            String commandName = mapping.commandName();
            CommandData commandData = commandProperties.getCommands().get(commandName);
            if (commandData == null) {
                throw new CommandMappingException("Missing command data for key=" + commandName);
            }
            RegisteredCommand registeredCommand = new RegisteredCommand(commandName, commandData, handler);

            String command;
            if(UNKNOWN_KEY.equals(commandName)) {
                command = UNKNOWN_KEY;
            }
            else {
                if (commandData.command() == null || commandData.command().isBlank()) {
                    throw new CommandMappingException("Missing command for key=" + commandName);
                }
                command = commandData.command();
            }

            handlersByCommand.put(command, registeredCommand);
        }
    }

    public List<CommandData> getMenuCommands() {
        return handlersByCommand.entrySet().stream()
        .filter(entry -> !UNKNOWN_KEY.equals(entry.getKey()))
        .map(entry -> entry.getValue().commandData())
        .filter(data -> data.description() != null && !data.description().isBlank())
        .toList();
    }

    public Optional<RegisteredCommand> findHandlerByCommand(String command) {
        return Optional.ofNullable(handlersByCommand.get(command));
    }

    public RegisteredCommand unknownCommand() {
        RegisteredCommand unknownCommand = handlersByCommand.get(UNKNOWN_KEY);

        if (unknownCommand == null) {
            throw new CommandMappingException("Unknown command handler is not registered");
        }
        return unknownCommand;
    }
    
}
