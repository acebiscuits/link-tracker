package backend.academy.linktracker.scrapper.repository.chatRepository.impl;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.repository.chatRepository.ChatRepository;
import lombok.RequiredArgsConstructor;

@Repository
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlChatRepository implements ChatRepository {

    private final JdbcTemplate template;
    
    @Transactional
    @Override
    public List<Long> getAllChats() {
        return template.queryForList("""
            SELECT telegram_chat_id 
                FROM chats
            """, Long.class);
    }

    @Transactional
    @Override
    public boolean exists(Long chatId) {
        Boolean exists = template.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM chats where telegram_chat_id = ?)
            """, Boolean.class, chatId);
        return Boolean.TRUE.equals(exists);
    }

    @Transactional
    @Override
    public boolean save(Long chatId) {
        return template.update("""
            INSERT INTO chats(telegram_chat_id) 
                VALUES(?)
            """, chatId) != 0;
    }

    @Transactional
    @Override
    public boolean delete(Long chatId) {
        return template.update("""
            DELETE FROM chats 
                WHERE telegram_chat_id = ?
            """, chatId) != 0;
    }
}
