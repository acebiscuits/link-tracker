package backend.academy.linktracker.scrapper.service.scrapping.impl;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.config.ScrapperProperties;
import backend.academy.linktracker.scrapper.model.dto.StackOverflowAnswersResponseDto;
import backend.academy.linktracker.scrapper.model.dto.StackOverflowCommentsResponseDto;
import backend.academy.linktracker.scrapper.model.dto.StackOverflowQuestionResponseDto;
import backend.academy.linktracker.scrapper.model.entity.DetectedUpdate;
import backend.academy.linktracker.scrapper.model.entity.LinkCheckResult;
import backend.academy.linktracker.scrapper.model.entity.LinkState;
import backend.academy.linktracker.scrapper.model.entity.UpdateType;
import backend.academy.linktracker.scrapper.service.scrapping.ScrappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service
@RequiredArgsConstructor
public class StackOverflowService implements ScrappingService {
    
    private final StackOverflowClient stackOverflowClient;

    private final ScrapperProperties sp;

    @Override
    public boolean supports(URI url) {
        return url.getHost() != null && url.getHost().equalsIgnoreCase("stackoverflow.com");
    }

    @Override
    public List<LinkCheckResult> fetchUpdates(List<LinkState> links, OffsetDateTime checkStartedAt) {
        List<LinkCheckResult> result = new ArrayList<>();

        for(int from = 0; from < links.size(); from += sp.batchSize()) {
            int to = Math.min(from + sp.batchSize(), links.size());

            List<LinkState> chunk = links.subList(from, to);

            try {
                result.addAll(checkChunk(chunk, checkStartedAt));
            } catch(Exception ex) {
                log.warn("Failed to process StackOverflow chunk, size={}", chunk.size(), ex);
            }

            result.addAll(chunk.stream().map(LinkCheckResult::failure).toList());
        }

        return result;
    }

    private List<LinkCheckResult> checkChunk(List<LinkState> links, OffsetDateTime checkStartedAt) {
        try {
            Map<Long, LinkState> linksByQuestionId = new HashMap<>();
            List<LinkCheckResult> result = new ArrayList<>();

            for(LinkState link : links) {
                try {
                    long questionId = parseQuestionId(link.url());
    
                    linksByQuestionId.put(questionId, link);
                } catch(Exception ex) {
                    log.warn( "Failed to parse StackOverflow link {}", link.url(), ex);
                    result.add(LinkCheckResult.failure(link));
                }
            }

            if (linksByQuestionId.isEmpty()) {
                return result;
            }

            List<Long> questionIds = List.copyOf(linksByQuestionId.keySet());

            StackOverflowQuestionResponseDto questions;

            try {
                questions = stackOverflowClient.fetchQuestions(questionIds);
            } catch(Exception ex) {
                log.warn("Failed to fetch StackOverflow questions for chunk", ex);

                result.addAll(linksByQuestionId.values()
                    .stream()
                    .map(LinkCheckResult::failure)
                    .toList());

                return result;
            }
                

            Map<Long, String> titles = new HashMap<>();

            for (StackOverflowQuestionResponseDto.QuestionItem question : questions.items()) {
                titles.put(question.questionId(), question.title());
            }

            Set<Long> failedLinkIds = new HashSet<>();
            for (Map.Entry<Long, LinkState> entry : linksByQuestionId.entrySet()) {
                if (!titles.containsKey(entry.getKey())) {
                    failedLinkIds.add(entry.getValue().id());
                }
            }
            long fromDate = links.stream()
                .map(LinkState::lastCheckedAt)
                .min(OffsetDateTime::compareTo)
                .orElseThrow()
                .toEpochSecond();

            long toDate = checkStartedAt.toEpochSecond();

            Map<Long, List<DetectedUpdate>> updatesByLinkId = new HashMap<>();

            for (LinkState link : links) {
                updatesByLinkId.put(link.id(), new ArrayList<>());
            }

            try {
                loadAnswers(questionIds, linksByQuestionId, titles, updatesByLinkId, fromDate, toDate, checkStartedAt, failedLinkIds);
    
                loadComments( questionIds, linksByQuestionId, titles, updatesByLinkId, fromDate, toDate, checkStartedAt, failedLinkIds);

            } catch (Exception ex) {
                log.warn("Failed to fetch StackOverflow events for chunk", ex);

                result.addAll(
                    linksByQuestionId.values()
                        .stream()
                        .map(LinkCheckResult::failure)
                        .toList());

                return result;
            }

            for (Map.Entry<Long, LinkState> entry : linksByQuestionId.entrySet()) {
                Long questionId = entry.getKey();
                LinkState link = entry.getValue();

                if (!titles.containsKey(questionId)) {
                    result.add(LinkCheckResult.failure(link));

                    continue;
                }

                result.add(LinkCheckResult.success(link, updatesByLinkId.get(link.id())));
            }

            return result;
        } catch(Exception ex) {
            log.warn("Failed to check StackOverflow chunk", ex);

            return links.stream().map(LinkCheckResult::failure).toList();
        }
    }

    private void loadAnswers( List<Long> questionIds, Map<Long, LinkState> linksByQuestionId, Map<Long, String> titles, Map<Long, List<DetectedUpdate>> updatesByLinkId, long fromDate, long toDate, OffsetDateTime checkStartedAt, Set<Long> failedLinkIds) {
        int page = 1;

        while (true) {
            StackOverflowAnswersResponseDto response = stackOverflowClient.fetchAnswers(questionIds, fromDate, toDate, page);

            for(StackOverflowAnswersResponseDto.AnswerItem answer : response.answers()) {
                LinkState link = linksByQuestionId.get(answer.questionId());

                if (link == null) {
                    continue;
                }

                try {
                    OffsetDateTime createdAt = fromEpochSeconds(answer.creationDate());
    
                    if (!isNewForLink(createdAt, link, checkStartedAt)) {
                        continue;
                    }
    
                    updatesByLinkId.get(link.id())
                        .add(DetectedUpdate.builder()
                            .type(UpdateType.STACKOVERFLOW_ANSWER)
                            .title(titles.get(answer.questionId()))
                            .author(answer.owner() == null ? null : answer.owner().displayName())
                            .createdAt(createdAt)
                            .preview(preview(answer.body(), sp.previewLength()))
                            .build()
                        );
                } catch (Exception ex) {
                    failedLinkIds.add(link.id());

                    log.warn("Failed to process StackOverflow answer for link {}", link.url(), ex);
                }
            }

            if (!Boolean.TRUE.equals(response.hasMore())) {
                break;
            }

            page++;
        }
    }

    private void loadComments( List<Long> questionIds, Map<Long, LinkState> linksByQuestionId, Map<Long, String> titles, Map<Long, List<DetectedUpdate>> updatesByLinkId, long fromDate, long toDate, OffsetDateTime checkStartedAt, Set<Long> failedLinkIds) {
        int page = 1;

        while (true) {
            StackOverflowCommentsResponseDto response =
                stackOverflowClient.fetchComments(
                    questionIds,
                    fromDate,
                    toDate,
                    page
                );

            for (StackOverflowCommentsResponseDto.CommentItem comment : response.items()) {
                Long questionId = comment.postId();

                LinkState link = linksByQuestionId.get(questionId);

                if (link == null) {
                    continue;
                }

                try {
                    
                    OffsetDateTime createdAt =
                        fromEpochSeconds(comment.creationDate());
    
                    if (!isNewForLink(createdAt, link, checkStartedAt)) {
                        continue;
                    }
    
                    updatesByLinkId
                        .get(link.id())
                        .add(
                            DetectedUpdate.builder()
                                .type(UpdateType.STACKOVERFLOW_COMMENT)
                                .title(titles.get(questionId))
                                .author(comment.owner() == null ? null : comment.owner().displayName())
                                .createdAt(createdAt)
                                .preview(preview(comment.body(), sp.previewLength()))
                                .build()
                        );
                } catch (Exception ex) {
                    failedLinkIds.add(link.id());

                    log.warn("Failed to process StackOverflow comment for link {}", link.url(), ex);
                }
            }

            if (!Boolean.TRUE.equals(response.hasMore())) {
                break;
            }

            page++;
        }
    }

    private boolean isNewForLink( OffsetDateTime createdAt, LinkState link, OffsetDateTime checkStartedAt) {
        return createdAt.isAfter(link.lastCheckedAt()) && !createdAt.isAfter(checkStartedAt);
    }

    private OffsetDateTime fromEpochSeconds(Long epochSeconds) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds),ZoneOffset.UTC);
    }

    private Long parseQuestionId(URI url) {
        String[] parts = url.getPath().split("/");

        for (int i = 0; i < parts.length; i++) {
            if ("questions".equals(parts[i]) && i + 1 < parts.length) {
                return Long.valueOf(parts[i + 1]);
            }
        }

        throw new IllegalArgumentException("Invalid StackOverflow question URL: " + url);
    }
}
