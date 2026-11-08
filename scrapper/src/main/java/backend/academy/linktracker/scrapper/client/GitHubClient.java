package backend.academy.linktracker.scrapper.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import backend.academy.linktracker.scrapper.exc.GitHubResponseException;
import backend.academy.linktracker.scrapper.model.dto.GithubIssuePullRequestResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component
@RequiredArgsConstructor
public class GitHubClient {
    
    private static final int PAGE_SIZE = 100;

    private final RestClient githubRestClient;

    public List<GithubIssuePullRequestResponseDto> fetchIssuesAndPullRequests(String owner, String repository, int page) {
        log.info("FETCHING UPDATE GITHUB");
        try{
            GithubIssuePullRequestResponseDto[] response = githubRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/repos/{owner}/{repository}/issues")
                .queryParam("state", "all")
                .queryParam("sort", "created")
                .queryParam("direction", "desc")
                .queryParam("per_page", 100)
                .queryParam("page", page)
                .build(owner, repository)
            )
            .retrieve()
            .body(GithubIssuePullRequestResponseDto[].class);

            if (response == null) {
                throw new GitHubResponseException(
                    "GitHub returned empty response for repository "
                        + owner + "/" + repository
                );
            }

            //тут обернуть ошибки 400..500 в свое исключение. и понять что если response == null
            log.info(
                "GitHub response for {}/{}: page={}, items={}",
                owner,
                repository,
                page,
                response.length
            );

            for (GithubIssuePullRequestResponseDto item : response) {
                log.info(
                    "GitHub item: title={}, createdAt={}",
                    item.title(),
                    item.createdAt()
                );
            }
            return Arrays.asList(response);
        }
        catch (RestClientException ex) {
            throw new GitHubResponseException(
                "GitHub request failed for repository "
                    + owner + "/" + repository, ex);
        }
    }
}
