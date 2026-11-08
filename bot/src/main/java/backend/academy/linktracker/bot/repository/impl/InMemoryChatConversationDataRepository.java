package backend.academy.linktracker.bot.repository.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.model.entity.ConversationData;
import backend.academy.linktracker.bot.repository.ChatConversationDataRepository;

@Component
public class InMemoryChatConversationDataRepository implements ChatConversationDataRepository {//чат - активная команда(+state)
    
    private final Map<Long, ConversationData> conversations = new ConcurrentHashMap<>();

    @Override
    public boolean checkIfChatIsRegistered(Long chatId) {
        return conversations.containsKey(chatId);
    }

    @Override
    public ConversationData get(Long chatId) {
        return conversations.get(chatId);
    }

    @Override
    public void set(Long chatId, ConversationData conversationData) {
        conversations.put(chatId, conversationData);
    }

    @Override
    public void remove(Long chatId) {
        conversations.remove(chatId);
    }
}
