package backend.academy.linktracker.scrapper.model.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GithubIssuePullRequestResponseDto
 */
public record GithubIssuePullRequestResponseDto(
    Long id,
    Integer number,
    String title,
    String body,
    User user,

    @JsonProperty("created_at")
    OffsetDateTime createdAt,

    @JsonProperty("pull_request")
    PullRequestMarker pullRequest
) {
    public boolean isPullRequest() {
        return pullRequest != null;
    }

    public record User(
        String login
    ) {
    }

    public record PullRequestMarker() {
    }
}