package backend.academy.linktracker.scrapper.repository.jpa;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import backend.academy.linktracker.scrapper.model.repository.jpa.entity.SubscriptionJpaEntity;

/**
 * JpaLinkRepository
 */
public interface JpaSubscriptionRepository extends JpaRepository<SubscriptionJpaEntity, Long> {

    @Query("""
            SELECT DISTINCT  s
            FROM SubscriptionJpaEntity s
            JOIN s.tags t
            WHERE s.chat.telegramChatId = :chatId
            AND t IN :tags
            """)
    public List<SubscriptionJpaEntity> findByChatIdAndTags(@Param("chatId") Long chatId, @Param("tags") List<String> tags);

    @Query("""
            SELECT s
            FROM SubscriptionJpaEntity s
            WHERE s.chat.telegramChatId = :chatId
            """)
    public List<SubscriptionJpaEntity> findAllByChatId(Long chatId);

    public boolean existsByChatTelegramChatIdAndLinkUrl(Long chatId, String url);

    public int deleteByChatTelegramChatId(Long chatId);

    public int deleteByChatTelegramChatIdAndLinkUrl(Long chatId, String url);

        @Query("""
            SELECT s.chat.telegramChatId
            FROM SubscriptionJpaEntity s
            WHERE s.link.id = :linkId
            """)
    List<Long> findChatIdsByLinkId(Long linkId);

    @Query("""
        SELECT s.chat.telegramChatId
        FROM SubscriptionJpaEntity s
        WHERE s.link.id = :linkId
        AND s.createdAt <= :eventCreatedAt
        """)
        List<Long> findChatIdsByLinkIdAndSubscribedBefore(
        @Param("linkId")
        Long linkId,

        @Param("eventCreatedAt")
        OffsetDateTime eventCreatedAt);
}