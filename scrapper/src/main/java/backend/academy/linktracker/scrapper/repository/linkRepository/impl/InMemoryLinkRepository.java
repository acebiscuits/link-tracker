package backend.academy.linktracker.scrapper.repository.linkRepository.impl;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.model.entity.LinkState;
import backend.academy.linktracker.scrapper.model.entity.TrackedLink;
import backend.academy.linktracker.scrapper.repository.linkRepository.LinkRepository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "IN_MEMORY")
public class InMemoryLinkRepository implements LinkRepository {

    private final AtomicLong idSequence = new AtomicLong(1);

    private final Map<URI, LinkEntry> linksByUrl = new ConcurrentHashMap<>();

    private record LinkEntry(
        LinkState state,
        Map<Long, SubscriptionData> subscriptions
    ) {
    }

    private record SubscriptionData(
        List<String> tags,
        List<String> filters,
        OffsetDateTime createdAt
    ) {
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrackedLink> findByChatIdAndTags(Long chatId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return findAllByChatId(chatId);
        }

        return linksByUrl.values()
            .stream()
            .filter(e -> e.subscriptions.containsKey(chatId))
            .filter(e -> {
                SubscriptionData subscription = e.subscriptions.get(chatId);
                return subscription.tags.stream().anyMatch(linkTag -> tags.stream().anyMatch(linkTag::contains));
            })
            .map(e -> toTrackedLink(e, chatId)).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrackedLink> findAllByChatId(Long chatId) {
        return linksByUrl.values().stream()
            .filter(e -> e.subscriptions.containsKey(chatId))
            .map(e -> toTrackedLink(e, chatId))
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByChatIdAndUrl(Long chatId, URI url) {
        LinkEntry entry = linksByUrl.get(url);

        return entry != null && entry.subscriptions.containsKey(chatId);
    }

    @Transactional
    @Override
    public TrackedLink save(Long chatId, URI url, List<String> tags, List<String> filters) {

        List<String> safeTags = tags == null ? List.of() : List.copyOf(tags);
        List<String> safeFilters = tags == null ? List.of() : List.copyOf(tags);

        OffsetDateTime now = OffsetDateTime.now();

        LinkEntry entry = linksByUrl.compute(url, (key, existing) -> {
            LinkEntry actualEntry = existing;

            if(actualEntry == null) {
                LinkState state = new LinkState(
                    idSequence.getAndIncrement(), url, now);
                    actualEntry = new LinkEntry(state, new ConcurrentHashMap<>());
            }

            actualEntry.subscriptions.put(chatId, new SubscriptionData(safeTags, safeFilters, now));

            return actualEntry;
        });

        return toTrackedLink(entry, chatId);
    }

    @Transactional
    @Override
    public Optional<TrackedLink> deleteByChatIdAndUrl(Long chatId, URI linkUrl) {
        if (linkUrl == null) {
            return Optional.empty();
        }

        AtomicReference<TrackedLink> removed = new AtomicReference<>();

        linksByUrl.computeIfPresent(
            linkUrl, (key, entry) -> {
                SubscriptionData subscription = entry.subscriptions().remove(chatId);
                if(subscription == null) {
                    return entry;
                }

                removed.set(
                    new TrackedLink(
                        entry.state().id(),
                        entry.state().url(),
                        subscription.tags(),
                        subscription.filters()
                    )
                );

                if (entry.subscriptions().isEmpty()) {
                    return null;
                }

                return entry;
            }
        );

        return Optional.ofNullable(removed.get());
    }

    @Transactional
    @Override
    public void deleteAllLinksByChatId(Long chatId) {
        linksByUrl.keySet().forEach(url -> 
            linksByUrl.computeIfPresent(url, (key, entry) -> {
                entry.subscriptions().remove(chatId);

                if (entry.subscriptions().isEmpty()) {
                    return null;
                }

                return entry;
            })
        );
    }

    @Override 
    public List<LinkState> findDueForCheck(OffsetDateTime dueBefore, long afterId, int limit) {
        return linksByUrl.values().stream()
            .filter(e -> !e.subscriptions().isEmpty())
            .map(LinkEntry::state)
            .filter(link -> link.id() > afterId)
            .filter(l -> l.lastCheckedAt().isAfter(dueBefore))
            .sorted((first, second) -> Long.compare(first.id(), second.id()))
            .limit(limit)
            .toList();
    }

    @Transactional
    @Override
    public void updateLastCheckedAt(Long linkId, OffsetDateTime checkedAt) {
        Optional<URI> url = linksByUrl.entrySet()
            .stream()
            .filter(entry ->
                Objects.equals(
                    entry.getValue().state().id(),
                    linkId
                )
            )
            .map(Map.Entry::getKey)
            .findFirst();

        if (url.isEmpty()) {
            return;
        }

        linksByUrl.computeIfPresent(
            url.get(),
            (key, entry) -> {
                LinkState oldState = entry.state();
                if (!checkedAt.isAfter(oldState.lastCheckedAt())) {
                    return entry;
                }

                LinkState newState = new LinkState(
                    oldState.id(),
                    oldState.url(),
                    checkedAt
                );

                return new LinkEntry(
                    newState,
                    entry.subscriptions()
                );
            }
        );
    }

    @Transactional
    @Override
    public List<Long> findChatIdsByLinkId(Long linkId) {
        return linksByUrl.values()
            .stream()
            .filter(e -> Objects.equals(e.state().id(), linkId))
            .findFirst()
            .map(entry -> List.copyOf(entry.subscriptions().keySet()))
            .orElseGet(List::of);
    }

    private TrackedLink toTrackedLink(LinkEntry entry, Long chatId) {
        SubscriptionData subscription = entry.subscriptions().get(chatId);

        if (subscription == null) {
            throw new IllegalStateException(//поменять на свой эксепшн
                "Subscription not found for chatId="
                    + chatId
                    + ", linkId="
                    + entry.state().id()
            );
        }

        return new TrackedLink(
            entry.state().id(),
            entry.state().url(),
            subscription.tags(),
            subscription.filters()
        );
    }

    @Override
    public List<Long> findChatIdsByLinkIdAndSubscribedBefore(
        Long linkId,
        OffsetDateTime eventCreatedAt
    ) {
        return linksByUrl.values()
            .stream()
            .filter(entry ->
                entry.state().id().equals(linkId))
            .findFirst()
            .map(entry ->
                entry.subscriptions()
                    .entrySet()
                    .stream()
                    .filter(subscription -> !subscription
                                                .getValue()
                                                .createdAt()
                                                .isAfter(eventCreatedAt))
                    .map(Map.Entry::getKey)
                    .toList()
            )
            .orElseGet(List::of);
    }
}
