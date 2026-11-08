package backend.academy.linktracker.bot.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;

import backend.academy.linktracker.bot.command.registry.CommandHandlerRegistry;
import backend.academy.linktracker.bot.model.entity.CommandData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!test")
@RequiredArgsConstructor
@Component
@Slf4j
public class TelegramCommandsRegistrar {

    private final CommandHandlerRegistry commandRegistry;
    private final TelegramBot bot;

    @EventListener(ApplicationReadyEvent.class)
    public void registerCommands() {
        List<CommandData> commandsData = commandRegistry.getMenuCommands();
        List<BotCommand> commandsToSet = new ArrayList<>(commandsData.size());
        
        for (CommandData command : commandsData) {
            commandsToSet.add(new BotCommand(command.command(), command.description()));
        }
        bot.execute(new SetMyCommands(commandsToSet.toArray(new BotCommand[commandsData.size()])));
    }
}
