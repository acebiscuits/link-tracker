package backend.academy.linktracker.scrapper.repository.linkRepository.impl;

import java.net.URI;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.model.entity.LinkState;
import backend.academy.linktracker.scrapper.model.entity.TrackedLink;
import backend.academy.linktracker.scrapper.repository.linkRepository.LinkRepository;
import lombok.RequiredArgsConstructor;

@Repository
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
public class SqlLinkRepository implements LinkRepository {
    
    private final NamedParameterJdbcTemplate namedTemplate;
    private final JdbcTemplate template;

    private final RowMapper<TrackedLink> trackedLinkMapper = (rs, rowNum) -> {
        return TrackedLink.builder()
        .id(rs.getLong("link_id"))
        .url(URI.create(rs.getString("url")))
        .tags(readStringArray(rs, "tags"))
        .filters(readStringArray(rs, "filters"))
        .build();
    };

    private final RowMapper<LinkState> linkStateMapper =
    (rs, rowNum) ->
        new LinkState(
            rs.getLong("link_id"),
            URI.create(rs.getString("url")),
            rs.getObject(
                "last_checked_at",
                OffsetDateTime.class
            )
        );

    private List<String> readStringArray(ResultSet rs, String columnName) throws SQLException {
        Array array = rs.getArray(columnName);

        if (array == null) return List.of();

        String[] values = (String[]) array.getArray();
        return List.of(values);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrackedLink> findByChatIdAndTags(
        Long chatId,
        List<String> tags
    ) {
        if (tags == null || tags.isEmpty()) {
            return findAllByChatId(chatId);
        }

        return namedTemplate.query("""
            SELECT
                l.id AS link_id,
                l.url,

                COALESCE(
                    array_agg(DISTINCT st_all.tag)
                        FILTER (WHERE st_all.tag IS NOT NULL),
                    ARRAY[]::text[]
                ) AS tags,

                COALESCE(
                    array_agg(DISTINCT sf_all.filter)
                        FILTER (WHERE sf_all.filter IS NOT NULL),
                    ARRAY[]::text[]
                ) AS filters

            FROM subscriptions s

            JOIN chats c
                ON c.id = s.chat_id

            JOIN links l
                ON l.id = s.link_id

            JOIN subscription_tags st_filter
                ON st_filter.subscription_id = s.id

            LEFT JOIN subscription_tags st_all
                ON st_all.subscription_id = s.id

            LEFT JOIN subscription_filters sf_all
                ON sf_all.subscription_id = s.id

            WHERE c.telegram_chat_id = :chatId
              AND st_filter.tag IN (:tags)

            GROUP BY
                l.id,
                l.url
            """,
            Map.of(
                "chatId", chatId,
                "tags", tags
            ),
            trackedLinkMapper
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrackedLink> findAllByChatId(Long chatId) {
        return template.query("""
            SELECT
                l.id AS link_id,
                l.url,

                COALESCE(
                    array_agg(DISTINCT st.tag)
                        FILTER (WHERE st.tag IS NOT NULL),
                    ARRAY[]::text[]
                ) AS tags,

                COALESCE(
                    array_agg(DISTINCT sf.filter)
                        FILTER (WHERE sf.filter IS NOT NULL),
                    ARRAY[]::text[]
                ) AS filters

            FROM subscriptions s

            JOIN links l
                ON l.id = s.link_id

            JOIN chats c
                ON c.id = s.chat_id

            LEFT JOIN subscription_tags st
                ON st.subscription_id = s.id

            LEFT JOIN subscription_filters sf
                ON sf.subscription_id = s.id

            WHERE c.telegram_chat_id = ?

            GROUP BY
                l.id,
                l.url
            """,
            trackedLinkMapper,
            chatId
        );
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByChatIdAndUrl(
        Long chatId,
        URI url
    ) {
        Boolean exists = template.queryForObject("""
            SELECT EXISTS(
                SELECT 1

                FROM subscriptions s

                JOIN chats c
                    ON c.id = s.chat_id

                JOIN links l
                    ON l.id = s.link_id

                WHERE c.telegram_chat_id = ?
                  AND l.url = ?
            )
            """,
            Boolean.class,
            chatId,
            url.toString()
        );

        return Boolean.TRUE.equals(exists);
    }

   @Transactional
    @Override
    public TrackedLink save(
        Long chatId,
        URI url,
        List<String> tags,
        List<String> filters
    ) {
        List<String> safeTags =
            tags == null ? List.of() : tags;

        List<String> safeFilters =
            filters == null ? List.of() : filters;

        template.update("""
            INSERT INTO links (url)
            VALUES (?)
            ON CONFLICT (url) DO NOTHING
            """,
            url.toString()
        );

        Long linkId = template.queryForObject("""
            SELECT id
            FROM links
            WHERE url = ?
            """,
            Long.class,
            url.toString()
        );

        template.update("""
            INSERT INTO chats (telegram_chat_id)
            VALUES (?)
            ON CONFLICT (telegram_chat_id) DO NOTHING
            """,
            chatId
        );

        Long dbChatId = template.queryForObject("""
            SELECT id
            FROM chats
            WHERE telegram_chat_id = ?
            """,
            Long.class,
            chatId
        );

        template.update("""
            INSERT INTO subscriptions (
                chat_id,
                link_id
            )
            VALUES (?, ?)

            ON CONFLICT (chat_id, link_id)
                DO NOTHING
            """,
            dbChatId,
            linkId
        );

        Long subscriptionId = template.queryForObject("""
            SELECT id
            FROM subscriptions
            WHERE chat_id = ?
              AND link_id = ?
            """,
            Long.class,
            dbChatId,
            linkId
        );

        for (String tag : safeTags) {
            template.update("""
                INSERT INTO subscription_tags (
                    subscription_id,
                    tag
                )
                VALUES (?, ?)

                ON CONFLICT DO NOTHING
                """,
                subscriptionId,
                tag
            );
        }

        for (String filter : safeFilters) {
            template.update("""
                INSERT INTO subscription_filters (
                    subscription_id,
                    filter
                )
                VALUES (?, ?)

                ON CONFLICT DO NOTHING
                """,
                subscriptionId,
                filter
            );
        }

        return TrackedLink.builder()
            .id(linkId)
            .url(url)
            .tags(safeTags)
            .filters(safeFilters)
            .build();
    }

    @Transactional
    @Override
    public void deleteAllLinksByChatId(Long chatId) {
        template.update("""
            DELETE FROM chats
            WHERE telegram_chat_id = ?
            """,
            chatId
        );
    }

    @Transactional
    @Override
    public Optional<TrackedLink> deleteByChatIdAndUrl(Long chatId, URI linkUrl) {
        int deleted = template.update("""
            DELETE FROM subscriptions s
            USING chats c, links l
            WHERE s.chat_id = c.id
            AND s.link_id = l.id
            AND c.telegram_chat_id = ?
            AND l.url = ?
            """,
            chatId,
            linkUrl.toString()
        );

        if (deleted == 0) {
            return Optional.empty();
        }

        return Optional.of(
            TrackedLink.builder()
                .url(linkUrl)
                .build()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<LinkState> findDueForCheck(
        OffsetDateTime dueBefore,
        long afterId,
        int limit
    ) {
        return template.query("""
            SELECT
                l.id AS link_id,
                l.url,
                l.last_checked_at

            FROM links l

            WHERE l.last_checked_at <= ?
              AND l.id > ?

              AND EXISTS (
                  SELECT 1
                  FROM subscriptions s
                  WHERE s.link_id = l.id
              )

            ORDER BY l.id

            LIMIT ?
            """,
            linkStateMapper,
            dueBefore,
            afterId,
            limit
        );
    }

    @Transactional
    @Override
    public void updateLastCheckedAt(
        Long linkId,
        OffsetDateTime checkedAt
    ) {
        template.update("""
            UPDATE links

            SET last_checked_at = ?

            WHERE id = ?
              AND last_checked_at < ?
            """,
            checkedAt,
            linkId,
            checkedAt
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findChatIdsByLinkId(Long linkId) {
        return template.queryForList("""
            SELECT c.telegram_chat_id

            FROM subscriptions s

            JOIN chats c
                ON s.chat_id = c.id

            WHERE s.link_id = ?
            """,
            Long.class,
            linkId
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findChatIdsByLinkIdAndSubscribedBefore(
        Long linkId,
        OffsetDateTime eventCreatedAt
    ) {
        return template.queryForList("""
            SELECT c.telegram_chat_id

            FROM subscriptions s

            JOIN chats c
                ON c.id = s.chat_id

            WHERE s.link_id = ?
            AND s.created_at <= ?
            """,
            Long.class,
            linkId,
            eventCreatedAt
        );
    }
}
