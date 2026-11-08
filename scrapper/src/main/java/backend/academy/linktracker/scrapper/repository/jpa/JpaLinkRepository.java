package backend.academy.linktracker.scrapper.repository.jpa;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import backend.academy.linktracker.scrapper.model.repository.jpa.entity.LinkJpaEntity;

/**
 * JpaLinkRepository
 */
public interface JpaLinkRepository extends JpaRepository<LinkJpaEntity, Long> {

    Optional<LinkJpaEntity> findByUrl(String url);

    @Query("""
        SELECT l
        FROM LinkJpaEntity l
        WHERE l.lastCheckedAt <= :dueBefore
        AND l.id > :afterId
        AND EXISTS (
            SELECT s.id
            FROM SubscriptionJpaEntity s
            WHERE s.link = l
        )
        ORDER BY l.id
            """)
    List<LinkJpaEntity> findDueForCheck(
        @Param("dueBefore")
        OffsetDateTime dueBefore,

        @Param("afterId")
        long afterId,

        Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE LinkJpaEntity l
        SET l.lastCheckedAt = :checkedAt
        WHERE l.id = :linkId
          AND l.lastCheckedAt < :checkedAt
        """)
    int updateLastCheckedAt(
        @Param("linkId")
        Long linkId,

        @Param("checkedAt")
        OffsetDateTime checkedAt
    );
}