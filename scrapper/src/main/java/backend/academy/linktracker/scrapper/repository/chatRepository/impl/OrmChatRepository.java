package backend.academy.linktracker.scrapper.repository.chatRepository.impl;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.model.repository.jpa.entity.ChatJpaEntity;
import backend.academy.linktracker.scrapper.repository.chatRepository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaChatRepository;
import lombok.RequiredArgsConstructor;

@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
@RequiredArgsConstructor
@Repository
public class OrmChatRepository implements ChatRepository {

    private final JpaChatRepository jpaChatRepository;
    
    @Override
    @Transactional
    public List<Long> getAllChats() {
        return jpaChatRepository.findAllTelegramChatIds();
    }

    @Transactional
    @Override
    public boolean exists(Long chatId) {
        return jpaChatRepository.existsByTelegramChatId(chatId);
    }

    @Transactional
    @Override
    public boolean save(Long chatId) {
        if (jpaChatRepository.existsByTelegramChatId(chatId)) {
            return false;
        }

        jpaChatRepository.save(new ChatJpaEntity(chatId));
        return true;
    }

    @Transactional
    @Override
    public boolean delete(Long chatId) {
        int deleted = jpaChatRepository.deleteByTelegramChatId(chatId);
        return deleted != 0;
    }
}
