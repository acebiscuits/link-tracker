package backend.academy.linktracker.scrapper.service.scrapping.impl;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.config.ScrapperProperties;
import backend.academy.linktracker.scrapper.model.dto.GithubIssuePullRequestResponseDto;
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
public class GitHubService implements ScrappingService {
    
    private final ScrapperProperties sp;

    private final GitHubClient gitHubClient;

    @Override
    public boolean supports(URI url) {
        return url.getHost() != null && url.getHost().equalsIgnoreCase("github.com");
    }

    @Override
    public List<LinkCheckResult> fetchUpdates(List<LinkState> links, OffsetDateTime checkStartedAt) {
        return links.stream().map(link -> checkLink(link, checkStartedAt)).toList();
    }

    private LinkCheckResult checkLink(LinkState link, OffsetDateTime checkStartedAt) {
        try {

            GitHubRepositoryRef ref = parseRepositoryRef(link.url());
            
            List<DetectedUpdate> updates = new ArrayList<>();
            
            int page = 1;
            boolean reachedOldEvents = false;
            
            while (!reachedOldEvents) {
                List<GithubIssuePullRequestResponseDto> response = gitHubClient.fetchIssuesAndPullRequests(ref.owner(), ref.repository(), page);

                if (response.isEmpty()) {
                    break;
                }

                for(GithubIssuePullRequestResponseDto dto : response) {
                    OffsetDateTime createdAt = dto.createdAt();
                    
                    if (createdAt == null) {
                        continue;
                    }
                    
                    if (!createdAt.isAfter(link.lastCheckedAt())) {
                        reachedOldEvents = true;
                        break;
                    }
                    
                    if (createdAt.isAfter(checkStartedAt)) {
                        continue;
                    }
                    
                    updates.add(DetectedUpdate.builder()
                        .type(dto.isPullRequest() ? UpdateType.GITHUB_PULL_REQUEST : UpdateType.GITHUB_ISSUE)
                        .title(dto.title())
                        .author(dto.user() == null ? null : dto.user().login())
                        .createdAt(createdAt)
                        .preview(preview(dto.body(), sp.previewLength()))
                        .build());
                }
                
                if (reachedOldEvents || response.size() < sp.batchSize()) {
                    break;
                }
                
                page++;
            }
            return LinkCheckResult.success(link, updates);
        } catch(Exception ex) {
            log.warn("Failure to check GitHub link {}", link.url(), ex);
            return LinkCheckResult.failure(link);
        }
    }

    private GitHubRepositoryRef parseRepositoryRef(URI url) {
        String[] parts = url.getPath().split("/");

        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid GitHub repository URL: " + url);
        }

        return new GitHubRepositoryRef(parts[1], parts[2]);
    }

    private record GitHubRepositoryRef(String owner, String repository) {
    }
}
