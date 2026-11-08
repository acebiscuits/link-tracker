package backend.academy.linktracker.scrapper.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import backend.academy.linktracker.scrapper.model.repository.jpa.entity.ChatJpaEntity;

/**
 * JpaChatRepository
 */
public interface JpaChatRepository extends JpaRepository<ChatJpaEntity, Long> {

    @Query("""
            SELECT c.telegramChatId
            FROM ChatJpaEntity c
            """)
    public List<Long> findAllTelegramChatIds();

    public int deleteByTelegramChatId(Long telegramChatId);

    public boolean existsByTelegramChatId(Long telegramChatId);

    public Optional<ChatJpaEntity> findByTelegramChatId(Long telegramChatId);
}