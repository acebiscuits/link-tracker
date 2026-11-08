package backend.academy.linktracker.scrapper.model.dto;

import java.util.List;

public record ApiErrorResponseDto(
    String description,
    String code,
    String exceptionName,
    String exceptionMessage,
    List<String> stacktrace
) {
}