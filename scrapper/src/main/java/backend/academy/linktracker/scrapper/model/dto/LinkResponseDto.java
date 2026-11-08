package backend.academy.linktracker.scrapper.model.dto;

import java.net.URI;
import java.util.List;

public record LinkResponseDto(//ответ на запрос бота по /links
    Long id,
    URI url,
    List<String> tags,
    List<String> filters
) {
}