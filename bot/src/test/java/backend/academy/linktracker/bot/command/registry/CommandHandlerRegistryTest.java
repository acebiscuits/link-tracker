package backend.academy.linktracker.bot.command.registry;

import backend.academy.linktracker.bot.annotation.CommandHandlerAnnotation;
import backend.academy.linktracker.bot.command.handler.BaseCommandHandler;
import backend.academy.linktracker.bot.config.CommandProperties;
import backend.academy.linktracker.bot.exc.CommandMappingException;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.CommandData;
import backend.academy.linktracker.bot.model.entity.RegisteredCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class CommandHandlerRegistryTest {

    @Mock
    private CommandProperties commandProperties;

    @Test
    @DisplayName("Should return only commands visible in menu")
    void test1() {
        CommandData visibleCommand = CommandData.builder()
            .command("/visible")
            .description("visible command")
            .build();

        CommandData nullableCommand = CommandData.builder().command("/nullable").build();

        CommandData hiddenCommand = CommandData.builder()
            .command("/hidden")
            .description(" ")
            .build();

        when(commandProperties.getCommands()).thenReturn(Map.of(
            "visible", visibleCommand,
            "nullable", nullableCommand,
            "hidden", hiddenCommand
        ));
        
        CommandHandlerRegistry registry = new CommandHandlerRegistry(
            List.of(new VisibleHandler(), new HiddenHandler(), new NullableHandler()),
            commandProperties
        );

        assertThat(registry.getMenuCommands()).containsExactly(visibleCommand);
    }

    @CommandHandlerAnnotation(commandName = "visible")
    private static class VisibleHandler implements BaseCommandHandler {
    
        @Override
        public BotResponseDto handle(UserMessageDto userMessageDto, CommandData commandData) {
            return null;
        }
    }

    @CommandHandlerAnnotation(commandName = "hidden")
    private static class HiddenHandler implements BaseCommandHandler {
    
        @Override
        public BotResponseDto handle(UserMessageDto userMessageDto, CommandData commandData) {
            return null;
        }
    }
    @CommandHandlerAnnotation(commandName = "nullable")
    private static class NullableHandler implements BaseCommandHandler {
    
        @Override
        public BotResponseDto handle(UserMessageDto userMessageDto, CommandData commandData) {
            return null;
        }
    }

    @Test
    @DisplayName("Should return handler by command")
    void test2() {
        CommandData visibleCommand = CommandData.builder()
            .command("/visible")
            .description("visible command")
            .build();

        when(commandProperties.getCommands()).thenReturn(Map.of(
            "visible", visibleCommand
        ));
        
        BaseCommandHandler handler = new VisibleHandler();

        CommandHandlerRegistry registry = new CommandHandlerRegistry(
            List.of(handler),
            commandProperties
        );

        assertThat(registry.findHandlerByCommand("/visible").get()).isEqualTo(new RegisteredCommand("visible", visibleCommand, handler));
    }

    @Test
    @DisplayName("Should return unknown command")
    void test3() {
        CommandData unknownCommandData = CommandData.builder().description(" ").build();

        when(commandProperties.getCommands()).thenReturn(Map.of(
            "unknown", unknownCommandData));

        BaseCommandHandler handler = new UnknownCommandHandker();

        CommandHandlerRegistry registry = new CommandHandlerRegistry(
            List.of(handler),
            commandProperties
        );
        
        assertThat(registry.unknownCommand()).isEqualTo(new RegisteredCommand("unknown", unknownCommandData, handler));
    }

    @CommandHandlerAnnotation(commandName = "unknown")
    private static class UnknownCommandHandker implements BaseCommandHandler {
    
        @Override
        public BotResponseDto handle(UserMessageDto userMessageDto, CommandData commandData) {
            return null;
        }
    }

    @Test
    @DisplayName("Shoul throw CommandMappingException if no Command mapping annotation")
    void test4() {
        BaseCommandHandler handler = new NoAnnotationHandler();

        assertThatThrownBy(() ->
            new CommandHandlerRegistry(List.of(handler), commandProperties))
            .isInstanceOf(CommandMappingException.class)
            .hasMessageContaining("Missing CommandMapping annotation for handler");
    }

    private static class  NoAnnotationHandler implements BaseCommandHandler {
    
        @Override
        public BotResponseDto handle(UserMessageDto userMessageDto, CommandData commandData) {
            return null;
        }
    }

    @Test
    @DisplayName("Should throw CommandMappingException if no command")
    void test5() {
        CommandData visibleCommand = CommandData.builder()
            .description("visible command")
            .build();

        when(commandProperties.getCommands()).thenReturn(Map.of(
            "visible", visibleCommand
        ));

        BaseCommandHandler handler = new VisibleHandler();

        assertThatThrownBy(() ->
            new CommandHandlerRegistry(List.of(handler), commandProperties))
            .isInstanceOf(CommandMappingException.class)
            .hasMessageContaining("Missing command for key");
    }

}
