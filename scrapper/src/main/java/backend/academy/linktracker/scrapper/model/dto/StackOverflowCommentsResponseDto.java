package backend.academy.linktracker.scrapper.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * StackOverflowCommentsResponseDto
 */
public record StackOverflowCommentsResponseDto(
    List<CommentItem> items,

    @JsonProperty("has_more")
    Boolean hasMore
) {
    public record CommentItem(
        @JsonProperty("comment_id")
        Long commentId,

        @JsonProperty("post_id")
        Long postId,

        Owner owner,

        @JsonProperty("creation_date")
        Long creationDate,

        String body
    ) {}

    public record Owner(
        @JsonProperty("display_name")
        String displayName
    ) {}
}