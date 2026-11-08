package backend.academy.linktracker.scrapper.model.dto;

import java.util.List;

public record ListLinksResponseDto(//дает список всез отслеживаемых ссылок
    List<LinkResponseDto> links,
    Integer size
) {
}