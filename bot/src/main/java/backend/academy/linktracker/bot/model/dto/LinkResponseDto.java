package backend.academy.linktracker.bot.model.dto;

import java.net.URI;
import java.util.List;

import lombok.Builder;

@Builder
public record LinkResponseDto(//ответ на запрос бота по /links
    Long id,
    URI url,
    List<String> tags,
    List<String> filters
) {
}