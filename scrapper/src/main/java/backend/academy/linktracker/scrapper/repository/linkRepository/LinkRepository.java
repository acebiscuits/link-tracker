package backend.academy.linktracker.scrapper.repository.linkRepository;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import backend.academy.linktracker.scrapper.model.entity.LinkState;
import backend.academy.linktracker.scrapper.model.entity.TrackedLink;

public interface LinkRepository {

    public List<TrackedLink> findByChatIdAndTags(Long chatId, List<String> tags);

    public List<TrackedLink> findAllByChatId(Long chatId);

    public boolean existsByChatIdAndUrl(Long chatId, URI url);

    public TrackedLink save(Long chatId, URI url, List<String> tags, List<String> filters);

    public Optional<TrackedLink> deleteByChatIdAndUrl(Long chatId, URI linkUrl);

    public void deleteAllLinksByChatId(Long chatId);

    public void updateLastCheckedAt(Long linkId, OffsetDateTime checkedAt);

    public List<Long> findChatIdsByLinkId(Long linkId);

    public List<LinkState> findDueForCheck(OffsetDateTime dueBefore, long afterId, int limit);

    public List<Long> findChatIdsByLinkIdAndSubscribedBefore(Long linkId, OffsetDateTime eventCreatedAt);
}