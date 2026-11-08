package backend.academy.linktracker.scrapper.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowQuestionResponseDto(
    List<QuestionItem> items
) {
    public QuestionItem firstQuestion() {
        return items == null || items.isEmpty() ? null : items.getFirst();
    }

    public record QuestionItem(
        @JsonProperty("question_id")
        Long questionId,

        String title
    ) {
    }
}