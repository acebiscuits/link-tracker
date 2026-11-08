package backend.academy.linktracker.bot.command;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.command.registry.CommandHandlerRegistry;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.dto.UserMessageDto;
import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.model.entity.ConversationState;
import backend.academy.linktracker.bot.model.entity.RegisteredCommand;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommandDispatcher {

    private final CommandHandlerRegistry commandHandlerRegistry;

    private final ChatConversationDataRepository chatConversationDataRegistry;

    public BotResponseDto handleUpdate(UserMessageDto messageDto) {
        if (!chatConversationDataRegistry.checkIfChatIsRegistered(messageDto.chatId())) {
            chatConversationDataRegistry.set(
                messageDto.chatId(), 
                ConversationData
                    .builder()
                    .state(ConversationState.IDLE)
                    .build()
            );    
        }

        ConversationData conversationData = chatConversationDataRegistry.get(messageDto.chatId());
        String text = messageDto.update().message().text();

        if (isCommand(text)) {
            RegisteredCommand registeredCommand = commandHandlerRegistry.findHandlerByCommand(text)
                .orElseGet(commandHandlerRegistry::unknownCommand); 
            
            chatConversationDataRegistry.set(
                messageDto.chatId(),
                ConversationData.builder()
                    .state(ConversationState.IDLE)
                    .build()
            );
                
            return registeredCommand.handler().handle(messageDto, registeredCommand.commandData());
        }
        if (conversationData.state() != ConversationState.IDLE) {
            RegisteredCommand registeredCommand = commandHandlerRegistry.findHandlerByCommand(conversationData.commandName())
                .orElseGet(commandHandlerRegistry::unknownCommand); 

            return registeredCommand.handler().handle(messageDto, registeredCommand.commandData());
        }

        RegisteredCommand unknownCommand = commandHandlerRegistry.unknownCommand();
        
        return unknownCommand.handler().handle(messageDto, unknownCommand.commandData());
    }

    private boolean isCommand(String text) {
        return text != null && text.trim().startsWith("/");
    }
}
