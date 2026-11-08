package backend.academy.linktracker.scrapper.model.dto;

import java.net.URI;

import jakarta.validation.constraints.NotNull;

public record RemoveLinkRequestDto(//удаление ссылки по урлу
    @NotNull
    URI link
) {
}