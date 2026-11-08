package backend.academy.linktracker.scrapper.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.config.ScrapperProperties;
import backend.academy.linktracker.scrapper.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.scrapper.model.entity.DetectedUpdate;
import backend.academy.linktracker.scrapper.model.entity.LinkCheckResult;
import backend.academy.linktracker.scrapper.model.entity.LinkState;
import backend.academy.linktracker.scrapper.model.entity.TrackedLink;
import backend.academy.linktracker.scrapper.model.entity.UpdateType;
import backend.academy.linktracker.scrapper.repository.linkRepository.LinkRepository;
import backend.academy.linktracker.scrapper.service.notification.NotificationSender;
import backend.academy.linktracker.scrapper.service.scrapping.ScrappingService;
import backend.academy.linktracker.scrapper.service.scrapping.factory.ScrappingServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinksCheckService {

    private final ScrapperProperties sp;

    private final ScrappingServiceFactory scrappingFactory;

    private final LinkRepository linkRepository;

    private final NotificationSender notificationSender;

    public void checkAllLinks() {
        
        OffsetDateTime runStartedAt = OffsetDateTime.now();

        OffsetDateTime dueBefore = runStartedAt.minus(Duration.ofMillis(sp.checkIntervalMs()));

        long afterId = 0;

        while (true) {
            List<LinkState> batch = linkRepository.findDueForCheck(dueBefore, afterId, sp.batchSize());

            if (batch.isEmpty()) {
                break;
            }

            checkBatch(batch, runStartedAt);

            afterId = batch.getLast().id();
        }
    }

    private void checkBatch(List<LinkState> batch, OffsetDateTime checkStartedAt) {
        Map<ScrappingService, List<LinkState>> linksByService = batch.stream()
                .collect(
                    Collectors.groupingBy(
                        link ->
                            scrappingFactory
                                .scrappingService(
                                    link.url()))
                );

        for (Map.Entry<ScrappingService, List<LinkState>> entry : linksByService.entrySet()) {
            try {
                List<LinkCheckResult> results = entry.getKey().fetchUpdates(entry.getValue(), checkStartedAt);

                for (LinkCheckResult result : results) {
                    processResult(result, checkStartedAt);
                }
            } catch(Exception ex) {
                log.warn("Failed to process links by service {}", entry.getKey().getClass().getSimpleName(), ex);
            }

        }
    }

    private void processResult(LinkCheckResult result, OffsetDateTime checkedAt) {
        log.info("PROCESSING RESULT");
        if (!result.successful()) {
            log.info("not successful");

            return;
        }

        try {
            for (DetectedUpdate update : result.updates()) {
                sendUpdate(result.link(), update);
            }

            linkRepository.updateLastCheckedAt(result.link().id(), checkedAt);
        } catch (Exception ex) {
            log.warn("Failed to process updates for link {}", result.link().url(), ex);
        }
    }

    private void sendUpdate(LinkState link, DetectedUpdate update) {
        log.info("SENDING UPDATE");
        List<Long> chatIds = linkRepository.findChatIdsByLinkIdAndSubscribedBefore(link.id(), update.createdAt());

        if (chatIds.isEmpty()) return;
        
        notificationSender.send(
            LinkUpdateRequestDto.builder()
                .id(link.id())
                .url(link.url().toString())
                .type(UpdateType.valueOf(update.type().name()))
                .title(update.title())
                .author(update.author())
                .createdAt(update.createdAt())
                .preview(update.preview())
                .tgChatIds(chatIds)
                .build()
        );
    }
}