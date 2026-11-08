package backend.academy.linktracker.scrapper.repository.linkRepository.impl;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.model.entity.LinkState;
import backend.academy.linktracker.scrapper.model.entity.TrackedLink;
import backend.academy.linktracker.scrapper.model.repository.jpa.entity.ChatJpaEntity;
import backend.academy.linktracker.scrapper.model.repository.jpa.entity.LinkJpaEntity;
import backend.academy.linktracker.scrapper.model.repository.jpa.entity.SubscriptionJpaEntity;
import backend.academy.linktracker.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.linkRepository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.mapper.JpaEntityToTrackedLinkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OrmLinkRepository
 */
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
@Repository
@Slf4j
@RequiredArgsConstructor
public class OrmLinkRepository implements LinkRepository {

    private final JpaSubscriptionRepository jpaSubscriptionRepository;

    private final JpaLinkRepository jpaLinkRepository;

    private final JpaChatRepository jpaChatRepository;

    private final JpaEntityToTrackedLinkMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<TrackedLink> findByChatIdAndTags(Long chatId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return findAllByChatId(chatId);
        }

        return jpaSubscriptionRepository.findByChatIdAndTags(chatId, tags)
            .stream()
            .map(mapper::subscriptionJpaEntityToTrackedLink)
            .toList();

    }
    
    @Transactional(readOnly = true)
    @Override
    public List<TrackedLink> findAllByChatId(Long chatId) {
        return jpaSubscriptionRepository.findAllByChatId(chatId)
            .stream()
            .map(mapper::subscriptionJpaEntityToTrackedLink)
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByChatIdAndUrl(Long chatId, URI url) {
        return jpaSubscriptionRepository.existsByChatTelegramChatIdAndLinkUrl(chatId, url.toString());
    }

    @Transactional
    @Override
    public TrackedLink save(Long chatId, URI url, List<String> tags, List<String> filters) {
        ChatJpaEntity chat = jpaChatRepository.findByTelegramChatId(chatId)
            .orElseGet(() -> jpaChatRepository.save(new ChatJpaEntity(chatId)));

        LinkJpaEntity link = jpaLinkRepository.findByUrl(url.toString())
            .orElseGet(() -> jpaLinkRepository.save(new LinkJpaEntity(url.toString())));

        List<String> safeTags = tags == null ? List.of() : tags;
        List<String> safeFilters = filters == null ? List.of() : filters;

        SubscriptionJpaEntity subscription = SubscriptionJpaEntity.builder()
            .chat(chat)
            .link(link)
            .tags(new ArrayList<>(safeTags))
            .filters(new ArrayList<>(safeFilters))
            .build();

        return mapper
            .subscriptionJpaEntityToTrackedLink(
                jpaSubscriptionRepository
                    .save(subscription));
    }

    @Transactional
    @Override
    public Optional<TrackedLink> deleteByChatIdAndUrl(Long chatId, URI linkUrl) {
        if (jpaSubscriptionRepository.deleteByChatTelegramChatIdAndLinkUrl(chatId, linkUrl.toString()) != 0) {
            return Optional.of(TrackedLink.builder()
                .url(linkUrl)
                .build());   
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public void deleteAllLinksByChatId(Long chatId) {
        jpaSubscriptionRepository.deleteByChatTelegramChatId(chatId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LinkState> findDueForCheck(OffsetDateTime dueBefore, long afterId, int limit) {
        return jpaLinkRepository
            .findDueForCheck(dueBefore, afterId, PageRequest.of(0, limit))
            .stream()
            .map(link ->
                new LinkState(
                    link.getId(),
                    URI.create(link.getUrl()),
                    link.getLastCheckedAt()
                )
            )
            .toList();
    }

    @Transactional
    @Override
    public void updateLastCheckedAt(Long linkId, OffsetDateTime checkedAt) {
        jpaLinkRepository.updateLastCheckedAt(linkId, checkedAt);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findChatIdsByLinkId(Long linkId) {
        return jpaSubscriptionRepository.findChatIdsByLinkId(linkId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findChatIdsByLinkIdAndSubscribedBefore(
        Long linkId,
        OffsetDateTime eventCreatedAt
    ) {
        return jpaSubscriptionRepository
            .findChatIdsByLinkIdAndSubscribedBefore(
                linkId,
                eventCreatedAt
            );
    }
}