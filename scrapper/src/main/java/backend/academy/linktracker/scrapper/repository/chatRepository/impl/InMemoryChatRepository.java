package backend.academy.linktracker.scrapper.repository.chatRepository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.repository.chatRepository.ChatRepository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "IN_MEMORY")
public class InMemoryChatRepository implements ChatRepository {
    
    private final Set<Long> chats = ConcurrentHashMap.newKeySet();

    @Transactional
    @Override
    public List<Long> getAllChats() {
        return new ArrayList<>(chats);
    }

    @Transactional
    @Override
    public boolean exists(Long chatId) {
        return chats.contains(chatId);
    }

    @Transactional
    @Override
    public boolean save(Long chatId) {
        return chats.add(chatId);
    }

    @Transactional
    @Override
    public boolean delete(Long chatId) {
        return chats.remove(chatId);
    }
}
