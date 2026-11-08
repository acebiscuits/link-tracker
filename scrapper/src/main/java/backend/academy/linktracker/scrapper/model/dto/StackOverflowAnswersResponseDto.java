package backend.academy.linktracker.scrapper.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * StackOverflowAnswersResponseDto
 */
public record StackOverflowAnswersResponseDto(
    List<AnswerItem> answers,

    @JsonProperty("has_more")
    Boolean hasMore
) {
    public record AnswerItem(
        @JsonProperty("answer_id")
        Long answerId,

        @JsonProperty("question_id")
        Long questionId,

        Owner owner,

        @JsonProperty("creation_date")
        Long creationDate,

        String body
    ) {

        public record Owner(
            @JsonProperty("display_name")
            String displayName
        ) {
        }
    }
}