package backend.academy.linktracker.bot.model.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ListLinksResponseDto(//дает список всез отслеживаемых ссылок
    List<LinkResponseDto> links
) {
}